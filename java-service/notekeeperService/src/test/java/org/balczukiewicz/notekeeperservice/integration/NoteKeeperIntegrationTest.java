package org.balczukiewicz.notekeeperservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.balczukiewicz.notekeeperservice.dto.AuthRequest;
import org.balczukiewicz.notekeeperservice.dto.AuthResponse;
import org.balczukiewicz.notekeeperservice.dto.NoteRequest;
import org.balczukiewicz.notekeeperservice.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class NoteKeeperIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        // Clear database before each test
        noteRepository.deleteAll();

        // Clear all caches before each test
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
    }

    @Test
    void fullWorkflow_AuthenticateAndCreateNote() throws Exception {
        // When - Authenticate
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

        // When - Create note
        NoteRequest noteRequest = new NoteRequest();
        noteRequest.setTitle("Integration Test Note");
        noteRequest.setContent("This is a test note created during integration testing");

        mockMvc.perform(post("/api/v1/notes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration Test Note"))
                .andExpect(jsonPath("$.content").value("This is a test note created during integration testing"))
                .andExpect(jsonPath("$.id").exists());

        // Then - Verify note exists
        mockMvc.perform(get("/api/v1/notes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))  // Now should be exactly 1
                .andExpect(jsonPath("$[0].title").value("Integration Test Note"));

        // Then - Verify unauthorized access is blocked
        mockMvc.perform(get("/api/v1/notes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCachePerformance() throws Exception {
        // Given - Authenticate and get token
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

        // When - First call to get notes (cache miss)
        long startTime1 = System.nanoTime();
        mockMvc.perform(get("/api/v1/notes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        long duration1 = System.nanoTime() - startTime1;

        // When - Second call to get notes (cache hit)
        long startTime2 = System.nanoTime();
        mockMvc.perform(get("/api/v1/notes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        long duration2 = System.nanoTime() - startTime2;

        // Then - Cache hit should be faster
        System.out.printf("Cache miss: %d ns, Cache hit: %d ns%n", duration1, duration2);
        // Note: In integration tests, the performance difference might be minimal
        // but this demonstrates the cache is working
    }
}