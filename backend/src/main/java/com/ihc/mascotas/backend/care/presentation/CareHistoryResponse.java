package com.ihc.mascotas.backend.care.presentation;

import com.ihc.mascotas.backend.care.domain.CareHistoryAction;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CareHistoryResponse(
    UUID id, UUID userId, UUID careRecordId, UUID petId, String petName, String care,
    CareHistoryAction action, LocalDate previousCareDate, LocalDate currentCareDate,
    Instant ocurredAt, String changeReason, String performedByName){ }