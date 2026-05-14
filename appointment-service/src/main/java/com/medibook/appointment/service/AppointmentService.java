package com.medibook.appointment.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.medibook.appointment.dto.AppointmentRequestDTO;
import com.medibook.appointment.dto.NotificationMessageDTO;
import com.medibook.appointment.entity.Appointment;
import com.medibook.appointment.producer.NotificationProducer;
import com.medibook.appointment.repository.AppointmentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final NotificationProducer notificationProducer;

    public Appointment bookAppointment(AppointmentRequestDTO dto) {

        Appointment appointment = Appointment.builder()
                .patientId(dto.getPatientId())
                .doctorId(dto.getDoctorId())
                .doctorName(dto.getDoctorName())
                .appointmentDate(dto.getAppointmentDate())
                .appointmentTime(dto.getAppointmentTime())
                .status("PENDING")
                .reason(dto.getReason())
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);

        NotificationMessageDTO messageDTO = NotificationMessageDTO.builder()
                .appointmentId(savedAppointment.getId())
                .patientId(savedAppointment.getPatientId())
                .doctorName(savedAppointment.getDoctorName())
                .message("Appointment booked successfully")
                .build();

        notificationProducer.sendNotification(messageDTO);

        return savedAppointment;
    }

    public List<Appointment> getAppointmentsByPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    public List<Appointment> getAppointmentsByDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }
    public Appointment updateStatus(Long id, String status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appointment.setStatus(status);

        return appointmentRepository.save(appointment);
    }
}