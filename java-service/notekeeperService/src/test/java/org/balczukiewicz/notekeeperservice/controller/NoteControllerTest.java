package org.balczukiewicz.notekeeperservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.balczukiewicz.notekeeperservice.dto.NoteRequest;
import org.balczukiewicz.notekeeperservice.dto.NoteResponse;
import org.balczukiewicz.notekeeperservice.service.NoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoteController.class)
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoteService noteService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void createNote_ValidRequest_ReturnsCreatedNote() throws Exception {
        // Given
        NoteRequest request = new NoteRequest();
        request.setTitle("Test Note");
        request.setContent("Test Content");

        NoteResponse response = new NoteResponse(1L, "Test Note", "Test Content");

        when(noteService.createNote(any(NoteRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/notes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Note"))
                .andExpect(jsonPath("$.content").value("Test Content"));
    }

    @Test
    @WithMockUser
    void getAllNotes_ReturnsAllNotes() throws Exception {
        // Given
        List<NoteResponse> notes = Arrays.asList(
                new NoteResponse(1L, "Note 1", "Content 1"),
                new NoteResponse(2L, "Note 2", "Content 2")
        );

        when(noteService.getAllNotes()).thenReturn(notes);

        // When & Then
        mockMvc.perform(get("/api/v1/notes")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Note 1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].title").value("Note 2"));
    }

    @Test
    @WithMockUser
    void getNoteById_ExistingId_ReturnsNote() throws Exception {
        // Given
        NoteResponse response = new NoteResponse(1L, "Test Note", "Test Content");

        when(noteService.getNoteById(eq(1L))).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/notes/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Note"))
                .andExpect(jsonPath("$.content").value("Test Content"));
    }

    @Test
    @WithMockUser
    void createNote_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Given
        NoteRequest request = new NoteRequest();
        request.setTitle("");
        request.setContent("");

        // When & Then
        mockMvc.perform(post("/api/v1/notes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}