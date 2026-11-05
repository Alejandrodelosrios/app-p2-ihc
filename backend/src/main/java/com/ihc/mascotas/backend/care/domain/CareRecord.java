package com.ihc.mascotas.backend.care.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CareRecord(
        UUID id,
        UUID userId,
        UUID petId,
        String care,
        LocalDate careDate,
        Instant createdAt,
        CareRecordStatus status,
        UUID assignedToUserId,
        Integer recurrenceIntervalDays
) {
}