package com.medibook.notification.service;

import com.medibook.notification.dto.NotificationMessageDTO;
import com.medibook.notification.entity.Notification;
import com.medibook.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void savesNotificationWithSentStatus() {
        NotificationMessageDTO dto = NotificationMessageDTO.builder()
                .appointmentId(8L)
                .patientId(9L)
                .doctorName("Dr. Messaging")
                .message("Appointment booked successfully")
                .build();

        notificationService.saveNotification(dto);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals("SENT", captor.getValue().getStatus());
        assertEquals(8L, captor.getValue().getAppointmentId());
    }
}
