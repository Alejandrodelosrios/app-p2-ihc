package com.ihc.mascotas.backend.pet.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PetRepository {

    Pet save(Pet pet);

    Optional<Pet> findActiveByIdAndOwnerId(
            UUID petId,
            UUID ownerId
    );

    Optional<Pet> findById(UUID petId);

    List<Pet> findAllByIds(Collection<UUID> petIds);

    List<Pet> findAllActiveByOwnerId(UUID ownerId);
}