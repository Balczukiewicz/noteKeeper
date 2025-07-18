package org.balczukiewicz.notekeeperservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.balczukiewicz.notekeeperservice.api.NoteApi;
import org.balczukiewicz.notekeeperservice.dto.NoteRequest;
import org.balczukiewicz.notekeeperservice.dto.NoteResponse;
import org.balczukiewicz.notekeeperservice.service.NoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class NoteController implements NoteApi {

    private final NoteService noteService;

    @Override
    public ResponseEntity<NoteResponse> createNote(NoteRequest request, String token) {
        log.info("Creating note with title: {}", request.getTitle());
        NoteResponse response = noteService.createNote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<List<NoteResponse>> getAllNotes() {
        log.info("Fetching all notes");
        List<NoteResponse> notes = noteService.getAllNotes();
        return ResponseEntity.ok(notes);
    }

    @Override
    public ResponseEntity<NoteResponse> getNoteById(Long id) {
        log.info("Fetching note with id: {}", id);
        NoteResponse response = noteService.getNoteById(id);
        return ResponseEntity.ok(response);
    }
}