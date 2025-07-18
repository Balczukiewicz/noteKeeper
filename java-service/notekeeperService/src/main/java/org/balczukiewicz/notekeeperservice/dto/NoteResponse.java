package org.balczukiewicz.notekeeperservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NoteResponse {
    private Long id;
    private String title;
    private String content;
}