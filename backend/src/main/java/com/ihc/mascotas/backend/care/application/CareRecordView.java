package com.ihc.mascotas.backend.care.application;

import com.ihc.mascotas.backend.care.domain.CareRecord;
import com.ihc.mascotas.backend.pet.domain.PetSpecies;

public record CareRecordView(
        CareRecord record,
        String petName,
        PetSpecies petSpecies,
        boolean petActive
) {
}