package com.medibook.notification.repository;

import com.medibook.notification.entity.Notification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void findsNotificationsByPatientId() {
        notificationRepository.save(Notification.builder()
                .appointmentId(55L)
                .patientId(66L)
                .doctorName("Dr. Repository")
                .message("Saved")
                .status("SENT")
                .build());

        assertEquals(1, notificationRepository.findByPatientId(66L).size());
    }
}
