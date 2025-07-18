package org.balczukiewicz.notekeeperservice.service.impl;

import org.balczukiewicz.notekeeperservice.dto.NoteRequest;
import org.balczukiewicz.notekeeperservice.dto.NoteResponse;
import org.balczukiewicz.notekeeperservice.entity.Note;
import org.balczukiewicz.notekeeperservice.exception.NoteNotFoundException;
import org.balczukiewicz.notekeeperservice.repository.NoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private NoteServiceImpl noteService;

    @Test
    void createNote_ValidRequest_ReturnsNoteResponse() {
        NoteRequest request = new NoteRequest();
        request.setTitle("Test Note");
        request.setContent("Test Content");
        Note savedNote = new Note();
        savedNote.setId(1L);
        savedNote.setTitle("Test Note");
        savedNote.setContent("Test Content");
        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);
        NoteResponse response = noteService.createNote(request);
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Note", response.getTitle());
        assertEquals("Test Content", response.getContent());
        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    void getAllNotes_ReturnsAllNotes() {
        Note note1 = new Note();
        note1.setId(1L);
        note1.setTitle("Note 1");
        note1.setContent("Content 1");
        Note note2 = new Note();
        note2.setId(2L);
        note2.setTitle("Note 2");
        note2.setContent("Content 2");
        List<Note> notes = Arrays.asList(note1, note2);
        when(noteRepository.findAllByOrderByIdDesc()).thenReturn(notes);
        List<NoteResponse> responses = noteService.getAllNotes();
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).getId());
        assertEquals("Note 1", responses.get(0).getTitle());
        verify(noteRepository, times(1)).findAllByOrderByIdDesc();
    }

    @Test
    void getNoteById_ExistingId_ReturnsNote() {
        Note note = new Note();
        note.setId(1L);
        note.setTitle("Test Note");
        note.setContent("Test Content");
        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));
        NoteResponse response = noteService.getNoteById(1L);
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Note", response.getTitle());
        assertEquals("Test Content", response.getContent());
        verify(noteRepository, times(1)).findById(1L);
    }

    @Test
    void getNoteById_NonExistingId_ThrowsException() {
        when(noteRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(NoteNotFoundException.class, () -> noteService.getNoteById(999L));
        verify(noteRepository, times(1)).findById(999L);
    }
}