package com.ihc.mascotas.backend.care.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CareHistoryEntry(
        UUID id,
        UUID userId,
        UUID careRecordId,
        String petName,
        String care,
        CareHistoryAction action,
        LocalDate previousCareDate,
        LocalDate currentCareDate,
        Instant occurredAt
) {
}
