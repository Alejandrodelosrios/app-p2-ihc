package com.ihc.mascotas.backend.analytics.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record TrackEventRequest(
        @NotBlank @Size(max = 60) String event,
        Map<String, String> props,
        @Size(max = 40) String sessionId
) {
}
