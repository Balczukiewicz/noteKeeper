package org.balczukiewicz.notekeeperservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.balczukiewicz.notekeeperservice.dto.NoteRequest;
import org.balczukiewicz.notekeeperservice.dto.NoteResponse;
import org.balczukiewicz.notekeeperservice.entity.Note;
import org.balczukiewicz.notekeeperservice.exception.NoteNotFoundException;
import org.balczukiewicz.notekeeperservice.repository.NoteRepository;
import org.balczukiewicz.notekeeperservice.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "notes", key = "'all-notes'")
    public List<NoteResponse> getAllNotes() {
        log.info("Fetching all notes from database (cache miss)");

        List<Note> notes = noteRepository.findAllByOrderByIdDesc();
        log.info("Found {} notes", notes.size());

        List<NoteResponse> response = notes.stream()
                .map(note -> new NoteResponse(note.getId(), note.getTitle(), note.getContent()))
                .collect(Collectors.toList());

        log.info("Caching {} notes for future requests", response.size());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "note-by-id", key = "#p0")
    public NoteResponse getNoteById(Long id) {
        log.info("Fetching note by id: {} (checking cache first)", id);

        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + id));

        log.info("Found note with id: {} - caching result", id);
        return new NoteResponse(note.getId(), note.getTitle(), note.getContent());
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "notes", key = "'all-notes'"),
            @CacheEvict(value = "note-by-id", allEntries = true)
    })
    public NoteResponse createNote(NoteRequest noteRequest) {
        log.info("Creating new note with title: {}", noteRequest.getTitle());

        Note note = new Note();
        note.setTitle(noteRequest.getTitle());
        note.setContent(noteRequest.getContent());

        Note savedNote = noteRepository.save(note);
        log.info("Created note with id: {} - clearing caches", savedNote.getId());

        return new NoteResponse(savedNote.getId(), savedNote.getTitle(), savedNote.getContent());
    }

}