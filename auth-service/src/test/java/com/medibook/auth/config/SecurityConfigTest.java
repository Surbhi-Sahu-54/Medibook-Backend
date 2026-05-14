package com.medibook.auth.config;

import com.medibook.auth.controller.AuthController;
import com.medibook.auth.repository.DoctorProfileRepository;
import com.medibook.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private DoctorProfileRepository doctorProfileRepository;

    @Test
    void publicDoctorEndpointRemainsAccessibleWithoutAuthentication() throws Exception {
        when(authService.getDoctorById(42L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/auth/doctors/42"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unknownProtectedPathIsRejectedForAnonymousUser() throws Exception {
        mockMvc.perform(get("/private-area"))
                .andExpect(status().is4xxClientError());
    }
}
