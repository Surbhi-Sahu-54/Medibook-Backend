package com.medibook.auth.repository;

import com.medibook.auth.entity.DoctorProfile;
import com.medibook.auth.entity.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {
    List<DoctorProfile> findByVerificationStatus(VerificationStatus status);
    Optional<DoctorProfile> findByUserId(Long userId);
    Optional<DoctorProfile> findByMedicalRegistrationNumber(String registrationNumber);
}
