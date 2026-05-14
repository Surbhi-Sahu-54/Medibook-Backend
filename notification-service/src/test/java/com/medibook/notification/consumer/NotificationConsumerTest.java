package com.medibook.notification.consumer;

import com.medibook.notification.dto.NotificationMessageDTO;
import com.medibook.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationConsumer notificationConsumer;

    @Test
    void forwardsQueuePayloadToNotificationService() {
        NotificationMessageDTO dto = NotificationMessageDTO.builder()
                .appointmentId(21L)
                .patientId(34L)
                .doctorName("Dr. Consumer")
                .message("Booked")
                .build();

        notificationConsumer.consume(dto);

        verify(notificationService).saveNotification(dto);
    }
}
