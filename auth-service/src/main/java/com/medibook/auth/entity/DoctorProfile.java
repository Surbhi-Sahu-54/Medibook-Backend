package com.medibook.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctor_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    private String fullName;
    private String email;
    private String phone;
    private String dateOfBirth;
    private String gender;

    // Professional details
    @Column(unique = true, nullable = false)
    private String medicalRegistrationNumber;

    @Column(nullable = false)
    private String degree;

    @Column(nullable = false)
    private String specialization;

    private Integer experience;
    private String hospitalName;
    private String clinicAddress;
    private Double consultationFees;
    private String languages;

    @Column(columnDefinition = "TEXT")
    private String bio;

    // Document file paths
    private String govtIdUrl;
    private String medicalLicenseUrl;
    private String degreeCertificateUrl;
    private String profilePhotoUrl;

    // Availability
    private String availableDays;
    private String availableSlots;

    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    private String adminRemark;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
