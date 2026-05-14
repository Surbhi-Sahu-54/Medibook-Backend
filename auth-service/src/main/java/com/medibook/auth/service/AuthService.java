package com.medibook.auth.service;

import com.medibook.auth.dto.AdminDoctorResponse;
import com.medibook.auth.dto.LoginRequest;
import com.medibook.auth.dto.LoginResponse;
import com.medibook.auth.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import com.medibook.auth.entity.DoctorProfile;
public interface AuthService {
    User register(User user);
    LoginResponse login(LoginRequest request);
    void verifyOtp(String email, String otp);
    void resendOtp(String email);

    // Doctor registration
    void registerDoctor(
            String name, String email, String password, String phone,
            String dateOfBirth, String gender,
            String medicalRegistrationNumber, String degree, String specialization,
            Integer experience, String hospitalName, String clinicAddress,
            Double consultationFees, String languages, String bio,
            String availableDays, String availableSlots,
            MultipartFile govtId, MultipartFile medicalLicense,
            MultipartFile degreeCertificate, MultipartFile profilePhoto
    );

    // Admin operations
    List<AdminDoctorResponse> getPendingDoctors();
    List<AdminDoctorResponse> getApprovedDoctors();
    List<AdminDoctorResponse> getRejectedDoctors();
    void approveDoctor(Long profileId, String approvedBy);
    void rejectDoctor(Long profileId, String reason, String rejectedBy);
    Optional<DoctorProfile> getDoctorById(Long id);
}