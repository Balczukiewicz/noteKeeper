package org.balczukiewicz.notekeeperservice.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.balczukiewicz.notekeeperservice.dto.NoteRequest;
import org.balczukiewicz.notekeeperservice.dto.ErrorResponse;
import org.balczukiewicz.notekeeperservice.dto.NoteResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "Notes", description = "Notes management APIs")
@RequestMapping("/api/v1/notes")
@SecurityRequirement(name = "Bearer Authentication")
public interface NoteApi {

    @Operation(summary = "Create a new note")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Note created successfully",
                    content = @Content(schema = @Schema(implementation = NoteResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping
    ResponseEntity<NoteResponse> createNote(
            @Valid @RequestBody NoteRequest request,
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String token
    );

    @Operation(summary = "Get all notes")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Notes retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping
    ResponseEntity<List<NoteResponse>> getAllNotes();

    @Operation(summary = "Get note by ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Note retrieved successfully",
                    content = @Content(schema = @Schema(implementation = NoteResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Note not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{id}")
    ResponseEntity<NoteResponse> getNoteById(@PathVariable Long id);
}