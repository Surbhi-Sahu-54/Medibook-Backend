package com.medibook.auth.repository;

import com.medibook.auth.entity.DoctorProfile;
import com.medibook.auth.entity.VerificationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class DoctorProfileRepositoryTest {

    @Autowired
    private DoctorProfileRepository doctorProfileRepository;

    @Test
    void findsDoctorByUserIdAndRegistrationNumber() {
        DoctorProfile profile = DoctorProfile.builder()
                .userId(91L)
                .fullName("Dr. Repository")
                .medicalRegistrationNumber("REG-91")
                .degree("MBBS")
                .specialization("Neurology")
                .verificationStatus(VerificationStatus.APPROVED)
                .build();

        doctorProfileRepository.save(profile);

        assertTrue(doctorProfileRepository.findByUserId(91L).isPresent());
        assertTrue(doctorProfileRepository.findByMedicalRegistrationNumber("REG-91").isPresent());
        assertEquals(1, doctorProfileRepository.findByVerificationStatus(VerificationStatus.APPROVED).size());
    }
}
