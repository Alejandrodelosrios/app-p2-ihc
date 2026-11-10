package com.ihc.mascotas.backend.care.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record RescheduleCareRecordRequest(
        @NotNull LocalDate newCareDate,
        @NotBlank String reason,
        @NotBlank String scope
) {
}
