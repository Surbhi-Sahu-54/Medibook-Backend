package com.medibook.notification.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.medibook.notification.config.RabbitMQConfig;
import com.medibook.notification.dto.NotificationMessageDTO;
import com.medibook.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void consume(NotificationMessageDTO dto) {

        System.out.println("Message received: " + dto);

        notificationService.saveNotification(dto);
    }
}