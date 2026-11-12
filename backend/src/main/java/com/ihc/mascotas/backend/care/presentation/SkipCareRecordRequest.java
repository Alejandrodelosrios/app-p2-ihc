package com.ihc.mascotas.backend.care.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SkipCareRecordRequest(
        @NotBlank @Size(max = 500) String reason
) {
}
