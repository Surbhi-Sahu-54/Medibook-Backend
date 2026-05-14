package com.medibook.notification.service;

import org.springframework.stereotype.Service;

import com.medibook.notification.dto.NotificationMessageDTO;
import com.medibook.notification.entity.Notification;
import com.medibook.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void saveNotification(NotificationMessageDTO dto) {

        Notification notification = Notification.builder()
                .appointmentId(dto.getAppointmentId())
                .patientId(dto.getPatientId())
                .doctorName(dto.getDoctorName())
                .message(dto.getMessage())
                .status("SENT")
                .build();

        notificationRepository.save(notification);

        System.out.println("Notification saved successfully");
    }
}