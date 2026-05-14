package com.medibook.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.medibook.auth.dto.LoginRequest;
import com.medibook.auth.dto.LoginResponse;
import com.medibook.auth.entity.DoctorProfile;
import com.medibook.auth.entity.User;
import com.medibook.auth.entity.VerificationStatus;
import com.medibook.auth.repository.DoctorProfileRepository;
import com.medibook.auth.repository.UserRepository;
import com.medibook.auth.util.JwtUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpService otpService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private DoctorProfileRepository doctorProfileRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void loginSuccess() {

        LoginRequest request = new LoginRequest();
        request.setEmail("surbhi@gmail.com");
        request.setPassword("123");

        User user = User.builder()
                .id(1L)
                .name("Surbhi")
                .email("surbhi@gmail.com")
                .password("encodedPassword")
                .role("PATIENT")
                .isEmailVerified(true)
                .build();

        when(userRepository.findByEmail("surbhi@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("123", "encodedPassword"))
                .thenReturn(true);

        when(jwtUtil.generateToken(user.getEmail(), user.getRole()))
                .thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Surbhi", response.getName());
        assertEquals(1L, response.getUserId());

        verify(userRepository, times(1)).findByEmail("surbhi@gmail.com");
    }
    @Test
    void invalidPassword() {

        LoginRequest request = new LoginRequest();
        request.setEmail("surbhi@gmail.com");
        request.setPassword("wrong");

        User user = User.builder()
                .email("surbhi@gmail.com")
                .password("encodedPassword")
                .isEmailVerified(true)
                .build();

        when(userRepository.findByEmail("surbhi@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("wrong", "encodedPassword"))
                .thenReturn(false);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid password", ex.getMessage());
    }

    @Test
    void userNotFound() {

        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@gmail.com");
        request.setPassword("123");

        when(userRepository.findByEmail("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.login(request)
        );

        assertEquals("User not found", ex.getMessage());
    }
    
    @Test
    void emailNotVerified() {

        LoginRequest request = new LoginRequest();
        request.setEmail("surbhi@gmail.com");
        request.setPassword("123");

        User user = User.builder()
                .email("surbhi@gmail.com")
                .password("encodedPassword")
                .isEmailVerified(false)
                .build();

        when(userRepository.findByEmail("surbhi@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("123", "encodedPassword"))
                .thenReturn(true);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.login(request)
        );

        assertEquals(
        	    "Email not verified. Please check your inbox for the OTP.",
        	    ex.getMessage()
        	);
    }
    
    @Test
    void verifyOtpSuccess() {

        User user = User.builder()
                .email("surbhi@gmail.com")
                .isEmailVerified(false)
                .build();

        when(userRepository.findByEmail("surbhi@gmail.com"))
                .thenReturn(Optional.of(user));

        when(otpService.verifyOtp("surbhi@gmail.com", "123456"))
                .thenReturn(true);

        assertDoesNotThrow(() ->
                authService.verifyOtp("surbhi@gmail.com", "123456")
        );

        verify(userRepository, times(1))
                .findByEmail("surbhi@gmail.com");

        verify(otpService, times(1))
                .verifyOtp("surbhi@gmail.com", "123456");
    }

    @Test
    void verifyOtpThrowsWhenOtpInvalid() {
        when(otpService.verifyOtp("surbhi@gmail.com", "000000")).thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.verifyOtp("surbhi@gmail.com", "000000")
        );

        assertEquals("Invalid or expired OTP", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resendOtpRequiresExistingUser() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.resendOtp("missing@example.com")
        );

        assertEquals("User not found", exception.getMessage());
        verify(otpService, never()).sendOtp(anyString());
    }

    @Test
    void resendOtpSendsOtpForExistingUser() {
        when(userRepository.findByEmail("patient@example.com"))
                .thenReturn(Optional.of(User.builder().email("patient@example.com").build()));

        authService.resendOtp("patient@example.com");

        verify(otpService).sendOtp("patient@example.com");
    }

    @Test
    void registerRejectsDuplicateEmail() {
        User user = User.builder()
                .email("taken@example.com")
                .password("secret")
                .build();

        when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(user));

        assertEquals("Email already registered. Please login or use a different email.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerEncodesPasswordAndSendsOtp() {
        User input = User.builder()
                .email("new@example.com")
                .password("plain")
                .build();
        User saved = User.builder()
                .id(12L)
                .email("new@example.com")
                .password("encoded")
                .isEmailVerified(false)
                .verificationStatus(VerificationStatus.APPROVED)
                .build();

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = authService.register(input);

        assertEquals(12L, result.getId());
        verify(userRepository).save(argThat(user ->
                "encoded".equals(user.getPassword())
                        && !user.isEmailVerified()
                        && user.getVerificationStatus() == VerificationStatus.APPROVED));
        verify(otpService).sendOtp("new@example.com");
    }

    @Test
    void doctorLoginPendingBranch() {
        LoginRequest request = new LoginRequest();
        request.setEmail("doctor@example.com");
        request.setPassword("secret");

        User user = User.builder()
                .email("doctor@example.com")
                .password("encoded")
                .role("DOCTOR")
                .isEmailVerified(true)
                .verificationStatus(VerificationStatus.PENDING)
                .build();

        when(userRepository.findByEmail("doctor@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(request));

        assertTrue(exception.getMessage().startsWith("DOCTOR_PENDING:"));
    }

    @Test
    void doctorLoginRejectedBranch() {
        LoginRequest request = new LoginRequest();
        request.setEmail("doctor@example.com");
        request.setPassword("secret");

        User user = User.builder()
                .email("doctor@example.com")
                .password("encoded")
                .role("DOCTOR")
                .isEmailVerified(true)
                .verificationStatus(VerificationStatus.REJECTED)
                .rejectionReason("Documents unclear")
                .build();

        when(userRepository.findByEmail("doctor@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(request));

        assertEquals("DOCTOR_REJECTED: Documents unclear", exception.getMessage());
    }

    @Test
    void approveDoctorUpdatesProfileAndUser() {
        DoctorProfile profile = DoctorProfile.builder()
                .id(4L)
                .userId(8L)
                .verificationStatus(VerificationStatus.PENDING)
                .build();
        User user = User.builder()
                .id(8L)
                .email("doctor@example.com")
                .name("Asha")
                .verificationStatus(VerificationStatus.PENDING)
                .build();

        when(doctorProfileRepository.findById(4L)).thenReturn(Optional.of(profile));
        when(userRepository.findById(8L)).thenReturn(Optional.of(user));

        authService.approveDoctor(4L, "Reviewer");

        assertEquals(VerificationStatus.APPROVED, profile.getVerificationStatus());
        assertEquals(VerificationStatus.APPROVED, user.getVerificationStatus());
        assertEquals("Reviewer", user.getVerifiedBy());
        assertNotNull(user.getVerifiedAt());
        verify(doctorProfileRepository).save(profile);
        verify(userRepository).save(user);
    }

    @Test
    void rejectDoctorUpdatesProfileAndUser() {
        DoctorProfile profile = DoctorProfile.builder()
                .id(4L)
                .userId(8L)
                .verificationStatus(VerificationStatus.PENDING)
                .build();
        User user = User.builder()
                .id(8L)
                .email("doctor@example.com")
                .name("Asha")
                .verificationStatus(VerificationStatus.PENDING)
                .build();

        when(doctorProfileRepository.findById(4L)).thenReturn(Optional.of(profile));
        when(userRepository.findById(8L)).thenReturn(Optional.of(user));

        authService.rejectDoctor(4L, "Expired license", "Reviewer");

        assertEquals(VerificationStatus.REJECTED, profile.getVerificationStatus());
        assertEquals("Expired license", profile.getAdminRemark());
        assertEquals(VerificationStatus.REJECTED, user.getVerificationStatus());
        assertEquals("Expired license", user.getRejectionReason());
        verify(doctorProfileRepository).save(profile);
        verify(userRepository).save(user);
    }

    @Test
    void getDoctorByIdFallsBackToUserIdLookup() {
        DoctorProfile profile = DoctorProfile.builder()
                .id(6L)
                .userId(12L)
                .fullName("Dr. Fallback")
                .build();

        when(doctorProfileRepository.findById(12L)).thenReturn(Optional.empty());
        when(doctorProfileRepository.findByUserId(12L)).thenReturn(Optional.of(profile));

        Optional<DoctorProfile> result = authService.getDoctorById(12L);

        assertTrue(result.isPresent());
        assertEquals(6L, result.get().getId());
    }

    @Test
    void registerDoctorStoresProfileAndNotifiesApplicant() {
        MultipartFile govtId = mock(MultipartFile.class);
        MultipartFile license = mock(MultipartFile.class);

        User saved = User.builder()
                .id(21L)
                .email("doctor@example.com")
                .name("Doctor")
                .build();

        when(userRepository.findByEmail("doctor@example.com")).thenReturn(Optional.empty());
        when(doctorProfileRepository.findByMedicalRegistrationNumber("REG-21")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(fileStorageService.storeFile(any(), eq("govt-ids"))).thenReturn("uploads/govt-ids/id.pdf");
        when(fileStorageService.storeFile(any(), eq("licenses"))).thenReturn("uploads/licenses/license.pdf");
        when(fileStorageService.storeFile(any(), eq("certificates"))).thenReturn(null);
        when(fileStorageService.storeFile(any(), eq("photos"))).thenReturn(null);

        authService.registerDoctor(
                "Doctor", "doctor@example.com", "secret", "9999999999",
                "1990-01-01", "F", "REG-21", "MBBS", "Cardiology",
                8, "Hospital", "Clinic", 600.0, "English", "Bio",
                "Monday", "10:00", govtId, license, null, null
        );

        verify(userRepository).save(argThat(user ->
                "DOCTOR".equals(user.getRole())
                        && user.isEmailVerified()
                        && user.getVerificationStatus() == VerificationStatus.PENDING));
        verify(doctorProfileRepository).save(argThat(profile ->
                Long.valueOf(21L).equals(profile.getUserId())
                        && "REG-21".equals(profile.getMedicalRegistrationNumber())
                        && profile.getVerificationStatus() == VerificationStatus.PENDING));
        verify(otpService).sendSimpleEmail(eq("doctor@example.com"), contains("Application Received"), contains("Doctor"));
    }

    @Test
    void registerDoctorRejectsDuplicateRegistrationNumber() {
        when(userRepository.findByEmail("doctor@example.com")).thenReturn(Optional.empty());
        when(doctorProfileRepository.findByMedicalRegistrationNumber("REG-21"))
                .thenReturn(Optional.of(DoctorProfile.builder().id(99L).build()));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.registerDoctor(
                "Doctor", "doctor@example.com", "secret", null,
                null, null, "REG-21", "MBBS", "Cardiology",
                null, null, null, null, null, null,
                null, null, null, null, null, null
        ));

        assertEquals("Medical registration number already exists.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getDoctorByIdReturnsDirectProfileAndEmptyWhenMissing() {
        DoctorProfile profile = DoctorProfile.builder().id(3L).userId(10L).build();
        when(doctorProfileRepository.findById(3L)).thenReturn(Optional.of(profile));

        assertEquals(3L, authService.getDoctorById(3L).orElseThrow().getId());

        when(doctorProfileRepository.findById(404L)).thenReturn(Optional.empty());
        when(doctorProfileRepository.findByUserId(404L)).thenReturn(Optional.empty());

        assertTrue(authService.getDoctorById(404L).isEmpty());
    }

    @Test
    void approveDoctorThrowsWhenProfileMissing() {
        when(doctorProfileRepository.findById(404L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.approveDoctor(404L, "Admin"));

        assertEquals("Doctor profile not found", exception.getMessage());
    }

    @Test
    void rejectDoctorThrowsWhenProfileMissing() {
        when(doctorProfileRepository.findById(404L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.rejectDoctor(404L, "No", "Admin"));

        assertEquals("Doctor profile not found", exception.getMessage());
    }
}
