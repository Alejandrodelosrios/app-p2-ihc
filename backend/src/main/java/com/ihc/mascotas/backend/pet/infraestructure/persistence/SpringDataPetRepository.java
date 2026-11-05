package com.ihc.mascotas.backend.pet.infraestructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataPetRepository
        extends JpaRepository<PetJpaEntity, UUID> {

    Optional<PetJpaEntity> findByIdAndOwnerIdAndDeletedAtIsNull(
            UUID id,
            UUID ownerId
    );

    List<PetJpaEntity> findAllByOwnerIdAndDeletedAtIsNullOrderByCreatedAtDesc(
            UUID ownerId
    );
}