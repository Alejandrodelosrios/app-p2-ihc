package com.ihc.mascotas.backend.care.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateCareRecordRequest(
        @NotBlank String petName,
        @NotBlank String care,
        @NotBlank String animalType,
        @NotNull LocalDate careDate,
        @Email @Size(max = 180) String assigneeEmail
) {
}
