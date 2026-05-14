package com.medibook.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.medibook.user.dto.UserRegistrationDTO;
import com.medibook.user.entity.User;
import com.medibook.user.repository.DoctorRepository;
import com.medibook.user.repository.PatientRepository;
import com.medibook.user.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void registerDoctorSuccess() {

        UserRegistrationDTO dto = new UserRegistrationDTO();
        dto.setEmail("doctor@gmail.com");
        dto.setPassword("123");
        dto.setRole("DOCTOR");
        dto.setFirstName("Raj");
        dto.setLastName("Sharma");
        dto.setSpecialization("Cardiology");
        dto.setConsultationFee(500.0);
        dto.setAvailability("Mon-Fri");

        User savedUser = User.builder()
                .id(1L)
                .email("doctor@gmail.com")
                .role("DOCTOR")
                .firstName("Raj")
                .lastName("Sharma")
                .build();

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        User result = userService.registerUser(dto);

        assertNotNull(result);
        assertEquals("DOCTOR", result.getRole());
        assertEquals("doctor@gmail.com", result.getEmail());

        verify(userRepository, times(1))
                .save(any(User.class));

        verify(doctorRepository, times(1))
                .save(any());

        verify(patientRepository, never())
                .save(any());
    }

    @Test
    void registerPatientSuccess() {

        UserRegistrationDTO dto = new UserRegistrationDTO();
        dto.setEmail("patient@gmail.com");
        dto.setPassword("123");
        dto.setRole("PATIENT");
        dto.setFirstName("Surbhi");
        dto.setLastName("Patel");
        dto.setPhoneNumber("9876543210");
        dto.setAddress("Pune");
        dto.setMedicalHistory("None");

        User savedUser = User.builder()
                .id(2L)
                .email("patient@gmail.com")
                .role("PATIENT")
                .firstName("Surbhi")
                .lastName("Patel")
                .build();

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        User result = userService.registerUser(dto);

        assertNotNull(result);
        assertEquals("PATIENT", result.getRole());

        verify(userRepository, times(1))
                .save(any(User.class));

        verify(patientRepository, times(1))
                .save(any());

        verify(doctorRepository, never())
                .save(any());
    }

    @Test
    void registerAdminSuccess() {

        UserRegistrationDTO dto = new UserRegistrationDTO();
        dto.setEmail("admin@gmail.com");
        dto.setPassword("123");
        dto.setRole("ADMIN");
        dto.setFirstName("Admin");
        dto.setLastName("User");

        User savedUser = User.builder()
                .id(3L)
                .email("admin@gmail.com")
                .role("ADMIN")
                .firstName("Admin")
                .lastName("User")
                .build();

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        User result = userService.registerUser(dto);

        assertNotNull(result);
        assertEquals("ADMIN", result.getRole());

        verify(userRepository, times(1))
                .save(any(User.class));

        verify(doctorRepository, never())
                .save(any());

        verify(patientRepository, never())
                .save(any());
    }
}