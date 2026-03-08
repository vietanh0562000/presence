package com.presence.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * PATCH /api/sessions/{id}/mood — update just the mood tag after reflection.
 */
@Data
class UpdateMoodRequest {
    @NotNull
    private String moodTag;
}
