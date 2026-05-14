package com.medibook.auth.controller;

import com.medibook.auth.dto.*;
import com.medibook.auth.entity.User;
import com.medibook.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import com.medibook.auth.entity.DoctorProfile;
import com.medibook.auth.repository.DoctorProfileRepository;
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private DoctorProfileRepository doctorProfileRepository;
    // Patient registration
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User saved = authService.register(user);
            return ResponseEntity.ok(Map.of("message", "Registration successful. Please verify your email.", "userId", saved.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Doctor registration (multipart)
    @PostMapping(value = "/doctor/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerDoctor(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String dateOfBirth,
            @RequestParam(required = false) String gender,
            @RequestParam String medicalRegistrationNumber,
            @RequestParam String degree,
            @RequestParam String specialization,
            @RequestParam(required = false) Integer experience,
            @RequestParam(required = false) String hospitalName,
            @RequestParam(required = false) String clinicAddress,
            @RequestParam(required = false) Double consultationFees,
            @RequestParam(required = false) String languages,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String availableDays,
            @RequestParam(required = false) String availableSlots,
            @RequestParam("govtId") MultipartFile govtId,
            @RequestParam("medicalLicense") MultipartFile medicalLicense,
            @RequestParam(value = "degreeCertificate", required = false) MultipartFile degreeCertificate,
            @RequestParam(value = "profilePhoto", required = false) MultipartFile profilePhoto) {
        try {
            authService.registerDoctor(name, email, password, phone, dateOfBirth, gender,
                    medicalRegistrationNumber, degree, specialization, experience,
                    hospitalName, clinicAddress, consultationFees, languages, bio,
                    availableDays, availableSlots, govtId, medicalLicense,
                    degreeCertificate, profilePhoto);
            return ResponseEntity.ok(Map.of("message",
                    "Your application has been submitted. You will be notified after admin review."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Login 
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }

    // OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
        try {
            authService.verifyOtp(request.getEmail(), request.getOtp());
            return ResponseEntity.ok(Map.of("message", "Email verified successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody ResendOtpRequest request) {
        try {
            authService.resendOtp(request.getEmail());
            return ResponseEntity.ok(Map.of("message", "OTP resent successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Admin Endpoints 	
    @GetMapping("/admin/doctors/pending")
    public ResponseEntity<?> pendingDoctors() {
        return ResponseEntity.ok(authService.getPendingDoctors());
    }

    @GetMapping("/admin/doctors/approved")
    public ResponseEntity<?> approvedDoctors() {
        return ResponseEntity.ok(authService.getApprovedDoctors());
    }

    @GetMapping("/admin/doctors/rejected")
    public ResponseEntity<?> rejectedDoctors() {
        return ResponseEntity.ok(authService.getRejectedDoctors());
    }

    @PostMapping("/admin/doctors/{id}/approve")
    public ResponseEntity<?> approveDoctor(@PathVariable Long id,
                                           @RequestParam(required = false, defaultValue = "Admin") String approvedBy) {
        try {
            authService.approveDoctor(id, approvedBy);
            return ResponseEntity.ok(Map.of("message", "Doctor approved successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/admin/doctors/{id}/reject")
    public ResponseEntity<?> rejectDoctor(@PathVariable Long id, @RequestBody RejectRequest request,
                                          @RequestParam(required = false, defaultValue = "Admin") String rejectedBy) {
        try {
            authService.rejectDoctor(id, request.getReason(), rejectedBy);
            return ResponseEntity.ok(Map.of("message", "Doctor rejected."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
        @GetMapping("/doctors/{id}")
        public ResponseEntity<?> getDoctorById(@PathVariable Long id) {
            System.out.println("[AuthController] GET /auth/doctors/" + id);
            return authService.getDoctorById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }
}
