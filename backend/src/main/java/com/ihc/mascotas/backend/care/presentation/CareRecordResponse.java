package com.ihc.mascotas.backend.care.presentation;

import com.ihc.mascotas.backend.care.domain.CareRecordStatus;
import com.ihc.mascotas.backend.pet.domain.PetSpecies;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CareRecordResponse(
        UUID id,
        UUID userId,
        UUID petId,
        String petName,
        PetSpecies petSpecies,
        boolean petActive,
        String care,
        LocalDate careDate,
        Instant createdAt,
        CareRecordStatus status,
        UUID assignedToUserId,
        Integer recurrenceIntervalDays
) {
}