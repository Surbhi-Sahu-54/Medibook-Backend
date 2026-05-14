package com.medibook.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibook.user.dto.UserRegistrationDTO;
import com.medibook.user.entity.User;
import com.medibook.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void registerReturnsCreatedUserPayload() throws Exception {
        UserRegistrationDTO request = new UserRegistrationDTO();
        request.setEmail("patient@example.com");
        request.setPassword("secret");
        request.setRole("PATIENT");
        request.setFirstName("Patient");
        request.setLastName("One");

        User saved = User.builder()
                .id(5L)
                .email("patient@example.com")
                .password("secret")
                .role("PATIENT")
                .firstName("Patient")
                .lastName("One")
                .build();

        when(userService.registerUser(any(UserRegistrationDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.role").value("PATIENT"));
    }
}
