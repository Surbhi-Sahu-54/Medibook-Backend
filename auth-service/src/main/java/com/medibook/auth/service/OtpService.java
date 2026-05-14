package com.medibook.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import com.medibook.auth.entity.Otp;
import com.medibook.auth.repository.OtpRepository;
import com.medibook.auth.util.OtpUtil;

@Service
public class OtpService {

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private EmailService emailService;

    // Send OTP
    public void sendOtp(String email) {

        String otp = OtpUtil.generateOtp();
        
        Otp otpEntity = otpRepository.findByEmail(email);
        if (otpEntity == null) {
            otpEntity = new Otp();
            otpEntity.setEmail(email);
        }
        
        otpEntity.setOtp(otp);
        otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        otpRepository.save(otpEntity);

        emailService.sendOtp(email, otp);
    }

    //  Verify OTP
    public boolean verifyOtp(String email, String otp) {

        Otp savedOtp = otpRepository.findByEmail(email);

        if (savedOtp == null) {
            return false;
        }

        // check OTP match + expiry
        return savedOtp.getOtp().equals(otp)
                && savedOtp.getExpiryTime().isAfter(LocalDateTime.now());
    }

    public void sendSimpleEmail(String email, String subject, String body) {
        emailService.sendSimpleEmail(email, subject, body);
    }
}