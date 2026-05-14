package com.medibook.appointment.producer;

import com.medibook.appointment.config.RabbitMQConfig;
import com.medibook.appointment.dto.NotificationMessageDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private NotificationProducer notificationProducer;

    @Test
    void sendsNotificationToConfiguredExchangeAndRoutingKey() {
        NotificationMessageDTO message = NotificationMessageDTO.builder()
                .appointmentId(8L)
                .patientId(9L)
                .doctorName("Dr. Messaging")
                .message("Appointment booked successfully")
                .build();

        notificationProducer.sendNotification(message);

        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                message
        );
    }
}
