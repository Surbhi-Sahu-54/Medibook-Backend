package com.medibook.user.service;

import com.medibook.user.dto.UserRegistrationDTO;
import com.medibook.user.entity.Doctor;
import com.medibook.user.entity.Patient;
import com.medibook.user.entity.User;
import com.medibook.user.repository.DoctorRepository;
import com.medibook.user.repository.PatientRepository;
import com.medibook.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public User registerUser(UserRegistrationDTO dto) {
        User user = User.builder()
                .email(dto.getEmail())
                .password(dto.getPassword()) // Note: in real app, encode password
                .role(dto.getRole())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .build();
        
        user = userRepository.save(user);

        if ("DOCTOR".equalsIgnoreCase(user.getRole())) {
            Doctor doctor = Doctor.builder()
                    .user(user)
                    .specialization(dto.getSpecialization())
                    .consultationFee(dto.getConsultationFee())
                    .availability(dto.getAvailability())
                    .build();
            doctorRepository.save(doctor);
        } else if ("PATIENT".equalsIgnoreCase(user.getRole())) {
            Patient patient = Patient.builder()
                    .user(user)
                    .phoneNumber(dto.getPhoneNumber())
                    .address(dto.getAddress())
                    .medicalHistory(dto.getMedicalHistory())
                    .build();
            patientRepository.save(patient);
        }

        return user;
    }
}
