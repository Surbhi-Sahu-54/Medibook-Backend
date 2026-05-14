package com.medibook.notification.controller;

import com.medibook.notification.entity.Notification;
import com.medibook.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationRepository notificationRepository;

    @Test
    void getAllNotificationsReturnsRepositoryPayload() throws Exception {
        Notification notification = Notification.builder()
                .id(1L)
                .patientId(7L)
                .appointmentId(4L)
                .doctorName("Dr. Rao")
                .message("Appointment booked")
                .status("SENT")
                .build();

        when(notificationRepository.findAll()).thenReturn(List.of(notification));

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("SENT"));
    }

    @Test
    void getByPatientIdReturnsOnlyPatientNotifications() throws Exception {
        Notification notification = Notification.builder()
                .id(2L)
                .patientId(9L)
                .message("Confirmed")
                .status("SENT")
                .build();

        when(notificationRepository.findByPatientId(9L)).thenReturn(List.of(notification));

        mockMvc.perform(get("/api/notifications/patient/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value(9))
                .andExpect(jsonPath("$[0].message").value("Confirmed"));
    }
}
