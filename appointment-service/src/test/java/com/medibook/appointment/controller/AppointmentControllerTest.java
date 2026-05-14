package com.medibook.appointment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibook.appointment.dto.AppointmentRequestDTO;
import com.medibook.appointment.entity.Appointment;
import com.medibook.appointment.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    @Test
    void bookReturnsSavedAppointment() throws Exception {
        AppointmentRequestDTO request = new AppointmentRequestDTO();
        request.setPatientId(7L);
        request.setDoctorId(3L);
        request.setDoctorName("Dr. Rajesh Sharma");
        request.setAppointmentDate(LocalDate.of(2026, 5, 20));
        request.setAppointmentTime(LocalTime.of(10, 0));
        request.setReason("Follow-up");

        Appointment appointment = Appointment.builder()
                .id(88L)
                .patientId(7L)
                .doctorId(3L)
                .doctorName("Dr. Rajesh Sharma")
                .appointmentDate(LocalDate.of(2026, 5, 20))
                .appointmentTime(LocalTime.of(10, 0))
                .status("PENDING")
                .reason("Follow-up")
                .build();

        when(appointmentService.bookAppointment(any(AppointmentRequestDTO.class))).thenReturn(appointment);

        mockMvc.perform(post("/api/appointments/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(88))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.doctorName").value("Dr. Rajesh Sharma"));
    }

    @Test
    void patientAppointmentsReturnListResponse() throws Exception {
        Appointment appointment = Appointment.builder()
                .id(31L)
                .patientId(7L)
                .doctorId(3L)
                .appointmentDate(LocalDate.of(2026, 5, 20))
                .appointmentTime(LocalTime.of(10, 0))
                .status("PENDING")
                .build();

        when(appointmentService.getAppointmentsByPatient(7L)).thenReturn(List.of(appointment));

        mockMvc.perform(get("/api/appointments/patient/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(31))
                .andExpect(jsonPath("$[0].patientId").value(7));
    }

    @Test
    void doctorAppointmentsReturnListResponse() throws Exception {
        Appointment appointment = Appointment.builder()
                .id(41L)
                .patientId(7L)
                .doctorId(3L)
                .appointmentDate(LocalDate.of(2026, 5, 20))
                .appointmentTime(LocalTime.of(10, 0))
                .status("CONFIRMED")
                .build();

        when(appointmentService.getAppointmentsByDoctor(3L)).thenReturn(List.of(appointment));

        mockMvc.perform(get("/api/appointments/doctor/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(41))
                .andExpect(jsonPath("$[0].doctorId").value(3));
    }

    @Test
    void updateStatusReturnsUpdatedAppointment() throws Exception {
        Appointment appointment = Appointment.builder()
                .id(88L)
                .patientId(7L)
                .doctorId(3L)
                .status("CONFIRMED")
                .build();

        when(appointmentService.updateStatus(88L, "CONFIRMED")).thenReturn(appointment);

        mockMvc.perform(put("/api/appointments/88/status").param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(88))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(appointmentService).updateStatus(88L, "CONFIRMED");
    }

    @Test
    void malformedBookingPayloadReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/appointments/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json"))
                .andExpect(status().isBadRequest());
    }
}
