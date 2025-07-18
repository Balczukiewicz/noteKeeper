package org.balczukiewicz.notekeeperservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.balczukiewicz.notekeeperservice.dto.AuthRequest;
import org.balczukiewicz.notekeeperservice.dto.AuthResponse;
import org.balczukiewicz.notekeeperservice.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void authenticate_ValidCredentials_ReturnsToken() throws Exception {
        // Given
        AuthRequest request = new AuthRequest();
        request.setUsername("admin");
        request.setPassword("password");

        AuthResponse response = AuthResponse
                .builder()
                .token("jwt-token")
                .expiresIn(86400000L).build();

        when(authService.authenticate(any(AuthRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/auth")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(86400000L));
    }

    @Test
    @WithMockUser
    void authenticate_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Given
        AuthRequest request = new AuthRequest();
        request.setUsername(""); // Invalid empty username
        request.setPassword("");

        // When & Then
        mockMvc.perform(post("/api/v1/auth")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}