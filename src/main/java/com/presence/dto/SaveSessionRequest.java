package com.presence.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * POST /api/sessions — save a completed or partial grounding session.
 */
@Data
public class SaveSessionRequest {

    /** Date of the session — if null the server uses today */
    private LocalDate date;

    @NotNull
    @Size(min = 1, message = "At least one answer is required")
    @Valid
    private List<SenseAnswerDto> answers;

    /** Mood tag selected at the end — optional */
    private String moodTag;

    /** Whether the session was completed fully */
    private boolean isPartial;

    /** Total questions in the app at the time of the session */
    private int totalQuestions;

    /** AI reflection text generated on the frontend — optional, saved for history */
    private String aiReflection;

    @Data
    public static class SenseAnswerDto {
        @NotNull
        private String sense;
        private String icon;
        private String question;
        @NotNull
        private String answer;
    }
}
