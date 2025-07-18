package org.balczukiewicz.notekeeperservice.service.impl;

import org.balczukiewicz.notekeeperservice.dto.NoteRequest;
import org.balczukiewicz.notekeeperservice.dto.NoteResponse;
import org.balczukiewicz.notekeeperservice.entity.Note;
import org.balczukiewicz.notekeeperservice.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Note Service Cache Tests")
class NoteServiceCacheTest {

    @Autowired
    private NoteServiceImpl noteService;

    @MockBean
    private NoteRepository noteRepository;

    @Autowired
    private CacheManager cacheManager;

    private Note testNote1;
    private Note testNote2;

    @BeforeEach
    void setUp() {
        // Clear all caches before each test
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });

        // Setup test data
        testNote1 = new Note();
        testNote1.setId(1L);
        testNote1.setTitle("Test Note 1");
        testNote1.setContent("Content 1");

        testNote2 = new Note();
        testNote2.setId(2L);
        testNote2.setTitle("Test Note 2");
        testNote2.setContent("Content 2");
    }

    @Test
    @DisplayName("Should cache getAllNotes result")
    void shouldCacheGetAllNotesResult() {
        // Given
        List<Note> mockNotes = Arrays.asList(testNote1, testNote2);
        when(noteRepository.findAllByOrderByIdDesc()).thenReturn(mockNotes);

        // When - First call
        List<NoteResponse> firstResult = noteService.getAllNotes();

        // When - Second call
        List<NoteResponse> secondResult = noteService.getAllNotes();

        // Then
        assertEquals(2, firstResult.size());
        assertEquals(2, secondResult.size());
        assertEquals(firstResult, secondResult);

        // Verify repository was called only once (cached on second call)
        verify(noteRepository, times(1)).findAllByOrderByIdDesc();

        // Verify cache contains the result
        var cache = cacheManager.getCache("notes");
        assertNotNull(cache);
        assertNotNull(cache.get("all-notes"));
    }

    @Test
    @DisplayName("Should cache getNoteById result")
    void shouldCacheGetNoteByIdResult() {
        // Given
        Long noteId = 1L;
        when(noteRepository.findById(noteId)).thenReturn(Optional.of(testNote1));

        // When - First call
        NoteResponse firstResult = noteService.getNoteById(noteId);

        // When - Second call
        NoteResponse secondResult = noteService.getNoteById(noteId);

        // Then
        assertEquals("Test Note 1", firstResult.getTitle());
        assertEquals(firstResult, secondResult);

        // Verify repository was called only once
        verify(noteRepository, times(1)).findById(noteId);

        // Verify cache contains the result
        var cache = cacheManager.getCache("note-by-id");
        assertNotNull(cache);
        assertNotNull(cache.get(noteId));
    }

    @Test
    @DisplayName("Should evict caches when creating note")
    void shouldEvictCachesWhenCreatingNote() {
        // Given - Populate caches first
        List<Note> mockNotes = Arrays.asList(testNote1);
        when(noteRepository.findAllByOrderByIdDesc()).thenReturn(mockNotes);
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote1));

        // Populate getAllNotes cache
        noteService.getAllNotes();
        // Populate getNoteById cache
        noteService.getNoteById(1L);

        // Verify caches are populated
        assertNotNull(cacheManager.getCache("notes").get("all-notes"));
        assertNotNull(cacheManager.getCache("note-by-id").get(1L));

        // Given - New note to create
        NoteRequest newNoteRequest = new NoteRequest();
        newNoteRequest.setTitle("New Note");
        newNoteRequest.setContent("New Content");

        Note savedNote = new Note();
        savedNote.setId(3L);
        savedNote.setTitle("New Note");
        savedNote.setContent("New Content");

        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        // When - Create note (should evict caches)
        NoteResponse result = noteService.createNote(newNoteRequest);

        // Then
        assertEquals("New Note", result.getTitle());
        assertEquals(3L, result.getId());

        // Verify caches are evicted
        assertNull(cacheManager.getCache("notes").get("all-notes"));
        assertNull(cacheManager.getCache("note-by-id").get(1L));
    }

    @Test
    @DisplayName("Should handle multiple cache keys independently")
    void shouldHandleMultipleCacheKeysIndependently() {
        // Given
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote1));
        when(noteRepository.findById(2L)).thenReturn(Optional.of(testNote2));

        // When - Cache different notes
        NoteResponse note1First = noteService.getNoteById(1L);
        NoteResponse note2First = noteService.getNoteById(2L);

        // Second calls (should hit cache)
        NoteResponse note1Second = noteService.getNoteById(1L);
        NoteResponse note2Second = noteService.getNoteById(2L);

        // Then
        assertEquals(note1First, note1Second);
        assertEquals(note2First, note2Second);

        // Verify each repository method called once per ID
        verify(noteRepository, times(1)).findById(1L);
        verify(noteRepository, times(1)).findById(2L);

        // Verify both items are cached
        var cache = cacheManager.getCache("note-by-id");
        assertNotNull(cache.get(1L));
        assertNotNull(cache.get(2L));
    }

    @Test
    @DisplayName("Should return empty list from cache when no notes exist")
    void shouldReturnEmptyListFromCacheWhenNoNotesExist() {
        // Given
        when(noteRepository.findAllByOrderByIdDesc()).thenReturn(Arrays.asList());

        // When
        List<NoteResponse> firstResult = noteService.getAllNotes();
        List<NoteResponse> secondResult = noteService.getAllNotes();

        // Then
        assertTrue(firstResult.isEmpty());
        assertTrue(secondResult.isEmpty());
        assertEquals(firstResult, secondResult);

        // Verify repository called only once
        verify(noteRepository, times(1)).findAllByOrderByIdDesc();
    }
}