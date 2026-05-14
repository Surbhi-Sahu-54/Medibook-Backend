package com.medibook.auth.dto;

import com.medibook.auth.entity.DoctorProfile;
import com.medibook.auth.entity.VerificationStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminDoctorResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String specialization;
    private String degree;
    private String medicalRegistrationNumber;
    private String hospitalName;
    private String clinicAddress;
    private Double consultationFees;
    private Integer experience;
    private String bio;
    private String govtIdUrl;
    private String medicalLicenseUrl;
    private String degreeCertificateUrl;
    private String profilePhotoUrl;
    private String availableDays;
    private String availableSlots;
    private VerificationStatus verificationStatus;
    private String adminRemark;
    private LocalDateTime createdAt;

    public static AdminDoctorResponse from(DoctorProfile p) {
        AdminDoctorResponse r = new AdminDoctorResponse();
        r.setId(p.getId());
        r.setUserId(p.getUserId());
        r.setFullName(p.getFullName());
        r.setEmail(p.getEmail());
        r.setPhone(p.getPhone());
        r.setSpecialization(p.getSpecialization());
        r.setDegree(p.getDegree());
        r.setMedicalRegistrationNumber(p.getMedicalRegistrationNumber());
        r.setHospitalName(p.getHospitalName());
        r.setClinicAddress(p.getClinicAddress());
        r.setConsultationFees(p.getConsultationFees());
        r.setExperience(p.getExperience());
        r.setBio(p.getBio());
        r.setGovtIdUrl(p.getGovtIdUrl());
        r.setMedicalLicenseUrl(p.getMedicalLicenseUrl());
        r.setDegreeCertificateUrl(p.getDegreeCertificateUrl());
        r.setProfilePhotoUrl(p.getProfilePhotoUrl());
        r.setAvailableDays(p.getAvailableDays());
        r.setAvailableSlots(p.getAvailableSlots());
        r.setVerificationStatus(p.getVerificationStatus());
        r.setAdminRemark(p.getAdminRemark());
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }
}
