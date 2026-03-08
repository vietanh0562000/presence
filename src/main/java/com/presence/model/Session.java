package com.presence.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * A single grounding session saved to MongoDB.
 *
 * MongoDB collection: "sessions"
 *
 * Example document:
 * {
 *   "_id": "...",
 *   "date": "2026-03-08",
 *   "answers": [ { "sense": "Sound", "answer": "Birds outside" }, ... ],
 *   "moodTag": "🌱 Grounded",
 *   "isPartial": false,
 *   "totalQuestions": 7,
 *   "createdAt": "2026-03-08T09:00:00Z",
 *   "updatedAt": "2026-03-08T09:05:00Z"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "sessions")
public class Session {

    @Id
    private String id;

    /** Owner of this session — indexed for fast per-user queries */
    @Indexed
    private String userId;

    /** ISO date of this session (e.g. "2026-03-08") — indexed for quick daily lookups */
    @Indexed
    private LocalDate date;

    /** All sense answers the user completed */
    private List<SenseAnswer> answers;

    /** The mood the user picked at the end (e.g. "🌊 Calm") — null if not chosen */
    private String moodTag;

    /** True if the user stopped before finishing all questions */
    private boolean isPartial;

    /** How many questions exist in total (for context if questions change over time) */
    private int totalQuestions;

    /** The AI-generated reflection text saved for history */
    private String aiReflection;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
