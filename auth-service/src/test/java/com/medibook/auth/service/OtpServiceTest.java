package com.medibook.auth.service;

import com.medibook.auth.entity.Otp;
import com.medibook.auth.repository.OtpRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private OtpService otpService;

    @Test
    void sendOtpCreatesOtpWhenEmailIsNew() {
        when(otpRepository.findByEmail("patient@example.com")).thenReturn(null);

        otpService.sendOtp("patient@example.com");

        ArgumentCaptor<Otp> captor = ArgumentCaptor.forClass(Otp.class);
        verify(otpRepository).save(captor.capture());
        assertTrue(captor.getValue().getOtp().matches("\\d{6}"));
        assertTrue(captor.getValue().getExpiryTime().isAfter(LocalDateTime.now()));
        verify(emailService).sendOtp("patient@example.com", captor.getValue().getOtp());
    }

    @Test
    void verifyOtpReturnsTrueForMatchingNonExpiredOtp() {
        Otp otp = new Otp();
        otp.setEmail("patient@example.com");
        otp.setOtp("123456");
        otp.setExpiryTime(LocalDateTime.now().plusMinutes(2));

        when(otpRepository.findByEmail("patient@example.com")).thenReturn(otp);

        assertTrue(otpService.verifyOtp("patient@example.com", "123456"));
    }

    @Test
    void verifyOtpReturnsFalseWhenOtpIsMissingOrExpired() {
        when(otpRepository.findByEmail("missing@example.com")).thenReturn(null);
        assertFalse(otpService.verifyOtp("missing@example.com", "123456"));

        Otp expired = new Otp();
        expired.setEmail("patient@example.com");
        expired.setOtp("123456");
        expired.setExpiryTime(LocalDateTime.now().minusMinutes(1));
        when(otpRepository.findByEmail("patient@example.com")).thenReturn(expired);

        assertFalse(otpService.verifyOtp("patient@example.com", "123456"));
    }
}
