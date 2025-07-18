package org.balczukiewicz.notekeeperservice.service;

import org.balczukiewicz.notekeeperservice.dto.NoteRequest;
import org.balczukiewicz.notekeeperservice.dto.NoteResponse;

import java.util.List;

public interface NoteService {

    NoteResponse createNote(NoteRequest request);

    List<NoteResponse> getAllNotes();

    NoteResponse getNoteById(Long id);

}