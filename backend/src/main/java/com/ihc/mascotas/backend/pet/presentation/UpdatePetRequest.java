package com.ihc.mascotas.backend.pet.presentation;

import com.ihc.mascotas.backend.pet.domain.PetSpecies;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdatePetRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String name,

        @NotNull(message = "La especie es obligatoria")
        PetSpecies species,

        @Size(max = 100)
        String breed,

        @PastOrPresent(message = "La fecha de nacimiento no puede ser futura")
        LocalDate birthDate

) {
}