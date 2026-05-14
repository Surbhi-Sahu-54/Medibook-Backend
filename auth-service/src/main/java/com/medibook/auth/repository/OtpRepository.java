package com.medibook.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.medibook.auth.entity.Otp;

public interface OtpRepository extends JpaRepository<Otp, Long> {
    Otp findByEmail(String email);
}