package com.medibook.appointment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.medibook.appointment.dto.AppointmentRequestDTO;
import com.medibook.appointment.entity.Appointment;
import com.medibook.appointment.producer.NotificationProducer;
import com.medibook.appointment.repository.AppointmentRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private NotificationProducer notificationProducer;

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    void bookAppointmentSuccess() {

        AppointmentRequestDTO dto = new AppointmentRequestDTO();
        dto.setPatientId(1L);
        dto.setDoctorId(101L);
        dto.setDoctorName("Dr Surbhi Sahu");
        dto.setAppointmentDate(LocalDate.now());
        dto.setAppointmentTime(LocalTime.of(10, 30));
        dto.setReason("Fever");

        Appointment savedAppointment = Appointment.builder()
                .patientId(1L)
                .doctorId(101L)
                .doctorName("Dr Surbhi Sahu")
                .appointmentDate(dto.getAppointmentDate())
                .appointmentTime(dto.getAppointmentTime())
                .status("PENDING")
                .reason("Fever")
                .build();

        when(appointmentRepository.save(any(Appointment.class)))
                .thenReturn(savedAppointment);

        Appointment result = appointmentService.bookAppointment(dto);

        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        assertEquals("Dr Surbhi Sahu", result.getDoctorName());
        assertEquals("Fever", result.getReason());

        verify(appointmentRepository, times(1))
                .save(any(Appointment.class));

        verify(notificationProducer, times(1))
                .sendNotification(any());
    }

    @Test
    void getAppointmentsByPatient() {

        List<Appointment> list = List.of(
                Appointment.builder()
                        .patientId(1L)
                        .doctorId(101L)
                        .status("PENDING")
                        .build()
        );

        when(appointmentRepository.findByPatientId(1L))
                .thenReturn(list);

        List<Appointment> result =
                appointmentService.getAppointmentsByPatient(1L);

        assertEquals(1, result.size());

        verify(appointmentRepository, times(1))
                .findByPatientId(1L);
    }

    @Test
    void getAppointmentsByDoctor() {

        List<Appointment> list = List.of(
                Appointment.builder()
                        .patientId(1L)
                        .doctorId(101L)
                        .status("PENDING")
                        .build()
        );

        when(appointmentRepository.findByDoctorId(101L))
                .thenReturn(list);

        List<Appointment> result =
                appointmentService.getAppointmentsByDoctor(101L);

        assertEquals(1, result.size());

        verify(appointmentRepository, times(1))
                .findByDoctorId(101L);
    }
    @Test
    void getAppointmentsByPatientEmptyList() {

        when(appointmentRepository.findByPatientId(1L))
                .thenReturn(List.of());

        List<Appointment> result =
                appointmentService.getAppointmentsByPatient(1L);

        assertTrue(result.isEmpty());

        verify(appointmentRepository, times(1))
                .findByPatientId(1L);
    }
    @Test
    void getAppointmentsByDoctorEmptyList() {

        when(appointmentRepository.findByDoctorId(101L))
                .thenReturn(List.of());

        List<Appointment> result =
                appointmentService.getAppointmentsByDoctor(101L);

        assertTrue(result.isEmpty());

        verify(appointmentRepository, times(1))
                .findByDoctorId(101L);
    }
    @Test
    void bookAppointmentRepositoryException() {

        AppointmentRequestDTO dto = new AppointmentRequestDTO();

        dto.setPatientId(1L);
        dto.setDoctorId(101L);

        when(appointmentRepository.save(any(Appointment.class)))
                .thenThrow(new RuntimeException("Database Error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> appointmentService.bookAppointment(dto)
        );

        assertEquals("Database Error", exception.getMessage());

        verify(appointmentRepository, times(1))
                .save(any(Appointment.class));
    }

    @Test
    void updateStatusPersistsNewStatus() {
        Appointment appointment = Appointment.builder()
                .id(20L)
                .patientId(1L)
                .doctorId(101L)
                .status("PENDING")
                .build();

        when(appointmentRepository.findById(20L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        Appointment result = appointmentService.updateStatus(20L, "CONFIRMED");

        assertEquals("CONFIRMED", result.getStatus());
        verify(appointmentRepository).findById(20L);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void updateStatusThrowsWhenAppointmentMissing() {
        when(appointmentRepository.findById(404L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> appointmentService.updateStatus(404L, "CANCELLED")
        );

        assertEquals("Appointment not found", exception.getMessage());
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }
}
