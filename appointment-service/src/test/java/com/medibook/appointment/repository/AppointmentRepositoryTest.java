package com.medibook.appointment.repository;

import com.medibook.appointment.entity.Appointment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class AppointmentRepositoryTest {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Test
    void findsAppointmentsByPatientAndDoctor() {
        appointmentRepository.save(Appointment.builder()
                .patientId(15L)
                .doctorId(22L)
                .doctorName("Dr. Repository")
                .appointmentDate(LocalDate.of(2026, 5, 22))
                .appointmentTime(LocalTime.of(11, 30))
                .status("PENDING")
                .build());

        assertEquals(1, appointmentRepository.findByPatientId(15L).size());
        assertEquals(1, appointmentRepository.findByDoctorId(22L).size());
    }
}
