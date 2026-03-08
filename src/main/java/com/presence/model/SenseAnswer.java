package com.presence.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One answered grounding question within a session.
 * Stored as an embedded array inside Session — no separate collection needed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SenseAnswer {

    /** e.g. "Air & Wind", "Sound", "Smell" */
    private String sense;

    /** Emoji icon shown in the UI e.g. "🌬" */
    private String icon;

    /** The full question text */
    private String question;

    /** What the user actually wrote (or "—" if skipped) */
    private String answer;
}
