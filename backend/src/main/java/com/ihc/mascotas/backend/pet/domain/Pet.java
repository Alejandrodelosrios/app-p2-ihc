package com.ihc.mascotas.backend.pet.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record Pet(
    UUID id,
        UUID ownerId,
        String name,
        PetSpecies species,
        String breed,
        LocalDate birthDate,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {
    public boolean isActive() {
        return deletedAt == null;
    }
}
    
