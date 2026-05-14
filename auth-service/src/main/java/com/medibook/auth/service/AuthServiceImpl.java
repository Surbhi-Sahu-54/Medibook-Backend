package com.medibook.auth.service;
import java.util.Optional;
import com.medibook.auth.dto.AdminDoctorResponse;
import com.medibook.auth.dto.LoginRequest;
import com.medibook.auth.dto.LoginResponse;
import com.medibook.auth.entity.DoctorProfile;
import com.medibook.auth.entity.User;
import com.medibook.auth.entity.VerificationStatus;
import com.medibook.auth.repository.DoctorProfileRepository;
import com.medibook.auth.repository.UserRepository;
import com.medibook.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private OtpService otpService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private DoctorProfileRepository doctorProfileRepository;
    @Autowired private FileStorageService fileStorageService;

    @Override
    public User register(User user) {
    	System.out.println(user);
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered. Please login or use a different email.");
        }
        user.setEmailVerified(false);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Patients are approved immediately; doctors via this path also approved
        // (Doctor-specific flow goes through registerDoctor())
        user.setVerificationStatus(VerificationStatus.APPROVED);
        User saved = userRepository.save(user);
        try {
            otpService.sendOtp(saved.getEmail());
        } catch (Exception e) {
        	e.printStackTrace();
            System.err.println("[AuthService] OTP email failed: " + e.getMessage());
        }
        return saved;
    }

    @Override
    public void registerDoctor(
            String name, String email, String password, String phone,
            String dateOfBirth, String gender,
            String medicalRegistrationNumber, String degree, String specialization,
            Integer experience, String hospitalName, String clinicAddress,
            Double consultationFees, String languages, String bio,
            String availableDays, String availableSlots,
            MultipartFile govtId, MultipartFile medicalLicense,
            MultipartFile degreeCertificate, MultipartFile profilePhoto) {

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered.");
        }
        if (doctorProfileRepository.findByMedicalRegistrationNumber(medicalRegistrationNumber).isPresent()) {
            throw new RuntimeException("Medical registration number already exists.");
        }

        // Create User with PENDING status — no email verification needed
        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role("DOCTOR")
                .isEmailVerified(true) // skip OTP for doctors
                .verificationStatus(VerificationStatus.PENDING)
                .build();
        User saved = userRepository.save(user);

        // Store uploaded files
        String govtIdUrl = fileStorageService.storeFile(govtId, "govt-ids");
        String licenseUrl = fileStorageService.storeFile(medicalLicense, "licenses");
        String certUrl = fileStorageService.storeFile(degreeCertificate, "certificates");
        String photoUrl = fileStorageService.storeFile(profilePhoto, "photos");

        // Create DoctorProfile
        DoctorProfile profile = DoctorProfile.builder()
                .userId(saved.getId())
                .fullName(name)
                .email(email)
                .phone(phone)
                .dateOfBirth(dateOfBirth)
                .gender(gender)
                .medicalRegistrationNumber(medicalRegistrationNumber)
                .degree(degree)
                .specialization(specialization)
                .experience(experience)
                .hospitalName(hospitalName)
                .clinicAddress(clinicAddress)
                .consultationFees(consultationFees)
                .languages(languages)
                .bio(bio)
                .availableDays(availableDays)
                .availableSlots(availableSlots)
                .govtIdUrl(govtIdUrl)
                .medicalLicenseUrl(licenseUrl)
                .degreeCertificateUrl(certUrl)
                .profilePhotoUrl(photoUrl)
                .verificationStatus(VerificationStatus.PENDING)
                .build();
        doctorProfileRepository.save(profile);

        // Notify doctor
        try {
            otpService.sendSimpleEmail(email, "MediBook Doctor Application Received",
                    "Dear Dr. " + name + ",\n\nYour doctor registration has been received. " +
                    "Our admin team will review your credentials and notify you once approved.\n\nThank you,\nMediBook Team");
        } catch (Exception e) {
        	e.printStackTrace();
            System.err.println("[AuthService] Notification email failed: " + e.getMessage());
        }
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        if (!user.isEmailVerified()) {
            throw new RuntimeException("Email not verified. Please check your inbox for the OTP.");
        }

        // Check doctor verification status
        if ("DOCTOR".equalsIgnoreCase(user.getRole())) {
            if (user.getVerificationStatus() == VerificationStatus.PENDING) {
                throw new RuntimeException("DOCTOR_PENDING: Your account is under review. You will be notified after admin approval.");
            }
            if (user.getVerificationStatus() == VerificationStatus.REJECTED) {
                String reason = user.getRejectionReason() != null ? user.getRejectionReason() : "Please contact support.";
                throw new RuntimeException("DOCTOR_REJECTED: " + reason);
            }
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return new LoginResponse(token, user.getName(), user.getId());
    }

    @Override
    public void verifyOtp(String email, String otp) {
        boolean isValid = otpService.verifyOtp(email, otp);
        if (!isValid) throw new RuntimeException("Invalid or expired OTP");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Override
    public void resendOtp(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        otpService.sendOtp(email);
    }

    // ── Admin methods ──────────────────

    @Override
    public List<AdminDoctorResponse> getPendingDoctors() {
        return doctorProfileRepository.findByVerificationStatus(VerificationStatus.PENDING)
                .stream().map(AdminDoctorResponse::from).collect(Collectors.toList());
    }

    @Override
    public List<AdminDoctorResponse> getApprovedDoctors() {
        return doctorProfileRepository.findByVerificationStatus(VerificationStatus.APPROVED)
                .stream().map(AdminDoctorResponse::from).collect(Collectors.toList());
    }

    @Override
    public List<AdminDoctorResponse> getRejectedDoctors() {
        return doctorProfileRepository.findByVerificationStatus(VerificationStatus.REJECTED)
                .stream().map(AdminDoctorResponse::from).collect(Collectors.toList());
    }

    @Override
    public void approveDoctor(Long profileId, String approvedBy) {
        DoctorProfile profile = doctorProfileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));
        profile.setVerificationStatus(VerificationStatus.APPROVED);
        doctorProfileRepository.save(profile);

        User user = userRepository.findById(profile.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setVerificationStatus(VerificationStatus.APPROVED);
        user.setVerifiedAt(LocalDateTime.now());
        user.setVerifiedBy(approvedBy != null ? approvedBy : "Admin");
        userRepository.save(user);

        try {
            otpService.sendSimpleEmail(user.getEmail(), "MediBook — Account Approved!",
                    "Dear Dr. " + user.getName() + ",\n\nCongratulations! Your MediBook doctor account has been verified. " +
                    "You can now login and start using MediBook.\n\nWelcome aboard!\nMediBook Team");
        } catch (Exception e) {
            System.err.println("[AuthService] Approval email failed: " + e.getMessage());
        }
    }

    @Override
    public void rejectDoctor(Long profileId, String reason, String rejectedBy) {
        DoctorProfile profile = doctorProfileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));
        profile.setVerificationStatus(VerificationStatus.REJECTED);
        profile.setAdminRemark(reason);
        doctorProfileRepository.save(profile);

        User user = userRepository.findById(profile.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setVerificationStatus(VerificationStatus.REJECTED);
        user.setRejectionReason(reason);
        userRepository.save(user);

        try {
            otpService.sendSimpleEmail(user.getEmail(), "MediBook — Verification Update",
                    "Dear Dr. " + user.getName() + ",\n\nUnfortunately your doctor verification was not approved.\n" +
                    "Reason: " + reason + "\n\nPlease contact support for more details.\n\nMediBook Team");
        } catch (Exception e) {
        	e.printStackTrace();
            System.err.println("[AuthService] Rejection email failed: " + e.getMessage());
        }
    }
    @Override
    public Optional<DoctorProfile> getDoctorById(Long id) {
        Optional<DoctorProfile> profile = doctorProfileRepository.findById(id);
        if (profile.isPresent()) {
            System.out.println("[AuthService] Doctor lookup matched profileId=" + id);
            return profile;
        }

        Optional<DoctorProfile> profileByUserId = doctorProfileRepository.findByUserId(id);
        if (profileByUserId.isPresent()) {
            System.out.println("[AuthService] Doctor lookup matched userId=" + id
                    + " -> profileId=" + profileByUserId.get().getId());
        } else {
            System.out.println("[AuthService] Doctor lookup found no profileId or userId for id=" + id);
        }
        return profileByUserId;
    }
}
