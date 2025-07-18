package org.balczukiewicz.notekeeperservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.balczukiewicz.notekeeperservice.dto.AuthRequest;
import org.balczukiewicz.notekeeperservice.dto.AuthResponse;
import org.balczukiewicz.notekeeperservice.dto.NoteRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should authenticate admin user successfully")
    void shouldAuthenticateAdminUserSuccessfully() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("admin");
        authRequest.setPassword("password");

        MvcResult result = mockMvc.perform(post("/api/v1/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600000L))
                .andReturn();

        AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class);
        assertNotNull(response.getToken());
        assertTrue(response.getToken().contains("."));
        assertEquals("Bearer", response.getType());
        assertEquals(3600000L, response.getExpiresIn());
    }

    @Test
    @DisplayName("Should authenticate regular user successfully")
    void shouldAuthenticateRegularUserSuccessfully() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("user");
        authRequest.setPassword("password");

        mockMvc.perform(post("/api/v1/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600000L));
    }

    @Test
    @DisplayName("Should reject authentication with wrong password")
    void shouldRejectAuthenticationWithWrongPassword() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("admin");
        authRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/v1/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication failed"))
                .andExpect(jsonPath("$.details").value("Invalid username or password"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Should reject authentication with non-existent user")
    void shouldRejectAuthenticationWithNonExistentUser() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("nonexistent");
        authRequest.setPassword("password");

        mockMvc.perform(post("/api/v1/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication failed"))
                .andExpect(jsonPath("$.details").value("Invalid username or password"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Should reject authentication with empty username")
    void shouldRejectAuthenticationWithEmptyUsername() throws Exception {
        // Given
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("");
        authRequest.setPassword("password");

        // When & Then
        mockMvc.perform(post("/api/v1/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("Should reject authentication with empty password")
    void shouldRejectAuthenticationWithEmptyPassword() throws Exception {
        // Given
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("admin");
        authRequest.setPassword("");

        // When & Then
        mockMvc.perform(post("/api/v1/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("Should reject authentication with null values")
    void shouldRejectAuthenticationWithNullValues() throws Exception {
        // Given
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(null);
        authRequest.setPassword(null);

        // When & Then
        mockMvc.perform(post("/api/v1/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("Should reject malformed JSON request")
    void shouldRejectMalformedJsonRequest() throws Exception {
        String malformedJson = "{\"username\":\"admin\",\"password\":}";

        mockMvc.perform(post("/api/v1/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject request without Content-Type header")
    void shouldRejectRequestWithoutContentTypeHeader() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("admin");
        authRequest.setPassword("password");

        mockMvc.perform(post("/api/v1/auth")
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("Should allow access to protected endpoint with valid token")
    void shouldAllowAccessToProtectedEndpointWithValidToken() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("admin");
        authRequest.setPassword("password");

        MvcResult authResult = mockMvc.perform(post("/api/v1/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
                authResult.getResponse().getContentAsString(), AuthResponse.class);
        String token = authResponse.getToken();

        mockMvc.perform(get("/api/v1/notes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should deny access to protected endpoint without token")
    void shouldDenyAccessToProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/notes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should deny access to protected endpoint with invalid token")
    void shouldDenyAccessToProtectedEndpointWithInvalidToken() throws Exception {
        String invalidToken = "invalid.jwt.token";

        mockMvc.perform(get("/api/v1/notes")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should deny access to protected endpoint with malformed Authorization header")
    void shouldDenyAccessToProtectedEndpointWithMalformedAuthHeader() throws Exception {
        String malformedHeader = "InvalidBearer token";

        mockMvc.perform(get("/api/v1/notes")
                        .header("Authorization", malformedHeader))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should successfully create note with valid token")
    void shouldSuccessfullyCreateNoteWithValidToken() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("admin");
        authRequest.setPassword("password");

        MvcResult authResult = mockMvc.perform(post("/api/v1/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
                authResult.getResponse().getContentAsString(), AuthResponse.class);
        String token = authResponse.getToken();

        NoteRequest noteRequest = new NoteRequest();
        noteRequest.setTitle("Auth Test Note");
        noteRequest.setContent("This note was created during authentication testing");

        mockMvc.perform(post("/api/v1/notes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Auth Test Note"))
                .andExpect(jsonPath("$.content").value("This note was created during authentication testing"));
    }

    @Test
    @DisplayName("Should handle multiple authentication requests")
    void shouldHandleMultipleAuthenticationRequests() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("admin");
        authRequest.setPassword("password");

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/v1/auth")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(authRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.type").value("Bearer"));
        }
    }

    @Test
    @DisplayName("Should generate different tokens for multiple authentication requests")
    void shouldGenerateDifferentTokensForMultipleAuthenticationRequests() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("admin");
        authRequest.setPassword("password");

        MvcResult result1 = mockMvc.perform(post("/api/v1/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Thread.sleep(1000);

        MvcResult result2 = mockMvc.perform(post("/api/v1/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse response1 = objectMapper.readValue(
                result1.getResponse().getContentAsString(), AuthResponse.class);
        AuthResponse response2 = objectMapper.readValue(
                result2.getResponse().getContentAsString(), AuthResponse.class);

        assertNotEquals(response1.getToken(), response2.getToken(),
                "Different authentication requests should generate different tokens");
    }
}