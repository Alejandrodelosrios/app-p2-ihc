package com.ihc.mascotas.backend.care.presentation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record CreateCareRecordRequest(
        @NotNull(message = "La mascota es obligatoria") UUID petId,
        @NotBlank @Size(max = 255) String care,
        @NotNull LocalDate careDate,
        @Email @Size(max = 180) String assigneeEmail,
        @Min(1) Integer recurrenceIntervalDays
) {
}