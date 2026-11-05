package com.ihc.mascotas.backend.pet.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PetRepository {

    Pet save(Pet pet);

    Optional<Pet> findActiveByIdAndOwnerId(
            UUID petId,
            UUID ownerId
    );

    List<Pet> findAllActiveByOwnerId(UUID ownerId);
}