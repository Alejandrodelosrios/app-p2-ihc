package com.ihc.mascotas.backend.care.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CareRecord(UUID id,
                         UUID userId,
                         String petName,
                         String care,
                         String animalType,
                         LocalDate careDate,
                         Instant createdAt,
                         CareRecordStatus status,
                         UUID assignedToUserId
                        ) {

}
