package com.ihc.mascotas.backend.pet.presentation;

import com.ihc.mascotas.backend.pet.domain.PetSpecies;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PetResponse(
        UUID id,
        UUID ownerId,
        String name,
        PetSpecies species,
        String breed,
        LocalDate birthDate,
        Instant createdAt,
        Instant updatedAt
) {
}