package com.medibook.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibook.auth.dto.LoginRequest;
import com.medibook.auth.dto.LoginResponse;
import com.medibook.auth.dto.VerifyOtpRequest;
import com.medibook.auth.entity.User;
import com.medibook.auth.entity.VerificationStatus;
import com.medibook.auth.entity.DoctorProfile;
import com.medibook.auth.repository.DoctorProfileRepository;
import com.medibook.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private DoctorProfileRepository doctorProfileRepository;

    @Test
    void loginReturnsTokenPayload() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("patient@example.com");
        request.setPassword("secret");

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse("jwt-token", "Patient One", 9L));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.name").value("Patient One"))
                .andExpect(jsonPath("$.userId").value(9));
    }

    @Test
    void registerReturnsCreatedUserMessage() throws Exception {
        User user = User.builder()
                .email("patient@example.com")
                .password("secret")
                .build();
        User saved = User.builder()
                .id(15L)
                .email("patient@example.com")
                .build();

        when(authService.register(any(User.class))).thenReturn(saved);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(15))
                .andExpect(jsonPath("$.message").value("Registration successful. Please verify your email."));
    }

    @Test
    void registerReturnsBadRequestWhenServiceRejectsUser() throws Exception {
        User user = User.builder()
                .email("taken@example.com")
                .password("secret")
                .build();

        when(authService.register(any(User.class))).thenThrow(new RuntimeException("Email already registered."));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already registered."));
    }

    @Test
    void loginReturnsUnauthorizedWhenServiceRejectsCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("patient@example.com");
        request.setPassword("wrong");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid password"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid password"));
    }

    @Test
    void verifyOtpReturnsSuccessMessage() throws Exception {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("patient@example.com");
        request.setOtp("123456");

        mockMvc.perform(post("/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email verified successfully."));
    }

    @Test
    void verifyOtpReturnsBadRequestForInvalidOtp() throws Exception {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("patient@example.com");
        request.setOtp("000000");

        doThrow(new RuntimeException("Invalid or expired OTP"))
                .when(authService).verifyOtp("patient@example.com", "000000");

        mockMvc.perform(post("/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired OTP"));
    }

    @Test
    void adminDoctorListsReturnStatusSpecificResponses() throws Exception {
        DoctorProfile profile = DoctorProfile.builder()
                .id(1L)
                .userId(3L)
                .fullName("Dr. Pending")
                .email("doctor@example.com")
                .medicalRegistrationNumber("REG-1")
                .degree("MBBS")
                .specialization("Cardiology")
                .verificationStatus(VerificationStatus.PENDING)
                .build();

        when(authService.getPendingDoctors()).thenReturn(List.of(com.medibook.auth.dto.AdminDoctorResponse.from(profile)));
        when(authService.getApprovedDoctors()).thenReturn(List.of());
        when(authService.getRejectedDoctors()).thenReturn(List.of());

        mockMvc.perform(get("/auth/admin/doctors/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
        mockMvc.perform(get("/auth/admin/doctors/approved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        mockMvc.perform(get("/auth/admin/doctors/rejected"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void approveAndRejectDoctorReturnMessages() throws Exception {
        mockMvc.perform(post("/auth/admin/doctors/7/approve").param("approvedBy", "Admin One"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Doctor approved successfully."));

        mockMvc.perform(post("/auth/admin/doctors/7/reject")
                        .param("rejectedBy", "Admin One")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Invalid license\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Doctor rejected."));
    }

    @Test
    void approveDoctorReturnsBadRequestWhenProfileIsMissing() throws Exception {
        doThrow(new RuntimeException("Doctor profile not found"))
                .when(authService).approveDoctor(eq(404L), eq("Admin"));

        mockMvc.perform(post("/auth/admin/doctors/404/approve"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Doctor profile not found"));
    }

    @Test
    void doctorMultipartRegistrationReturnsSuccess() throws Exception {
        mockMvc.perform(multipart("/auth/doctor/register")
                        .file("govtId", "id".getBytes())
                        .file("medicalLicense", "license".getBytes())
                        .param("name", "Dr. Multipart")
                        .param("email", "doctor@example.com")
                        .param("password", "secret")
                        .param("medicalRegistrationNumber", "REG-M")
                        .param("degree", "MBBS")
                        .param("specialization", "Cardiology"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Your application has been submitted. You will be notified after admin review."));
    }

    @Test
    void doctorLookupReturnsProfileWhenFound() throws Exception {
        DoctorProfile profile = DoctorProfile.builder()
                .id(12L)
                .userId(44L)
                .fullName("Dr. Profile")
                .medicalRegistrationNumber("REG-12")
                .degree("MBBS")
                .specialization("Cardiology")
                .build();

        when(authService.getDoctorById(12L)).thenReturn(Optional.of(profile));

        mockMvc.perform(get("/auth/doctors/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.fullName").value("Dr. Profile"));
    }

    @Test
    void doctorLookupReturnsNotFoundWhenMissing() throws Exception {
        when(authService.getDoctorById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/auth/doctors/99"))
                .andExpect(status().isNotFound());
    }
}
