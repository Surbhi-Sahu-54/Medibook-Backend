package com.medibook.user.dto;

import lombok.Data;

@Data
public class UserRegistrationDTO {
    private String email;
    private String password;
    private String role;
    private String firstName;
    private String lastName;
    // For Doctors
    private String specialization;
    private Double consultationFee;
    private String availability;
    // For Patients
    private String phoneNumber;
    private String address;
    private String medicalHistory;
}
