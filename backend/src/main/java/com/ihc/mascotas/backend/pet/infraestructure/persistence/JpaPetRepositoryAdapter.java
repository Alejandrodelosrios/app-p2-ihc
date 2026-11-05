package com.ihc.mascotas.backend.pet.infraestructure.persistence;

import com.ihc.mascotas.backend.pet.domain.Pet;
import com.ihc.mascotas.backend.pet.domain.PetRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public class JpaPetRepositoryAdapter implements PetRepository {

    private final SpringDataPetRepository repository;

    public JpaPetRepositoryAdapter(
            SpringDataPetRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public Pet save(Pet pet) {
        PetJpaEntity entity = new PetJpaEntity(
                pet.id(),
                pet.ownerId(),
                pet.name(),
                pet.species(),
                pet.breed(),
                pet.birthDate(),
                pet.createdAt(),
                pet.updatedAt(),
                pet.deletedAt()
        );

        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<Pet> findActiveByIdAndOwnerId(
            UUID petId,
            UUID ownerId
    ) {
        return repository
                .findByIdAndOwnerIdAndDeletedAtIsNull(petId, ownerId)
                .map(this::toDomain);
    }

    @Override
    public List<Pet> findAllActiveByOwnerId(UUID ownerId) {
        return repository
                .findAllByOwnerIdAndDeletedAtIsNullOrderByCreatedAtDesc(ownerId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Pet> findById(UUID petId) {
     return repository.findById(petId).map(this::toDomain);
    }
    
    @Override
    public List<Pet> findAllByIds(Collection<UUID> petIds) {
    return repository.findAllById(petIds)
            .stream()
            .map(this::toDomain)
            .toList();
}

    private Pet toDomain(PetJpaEntity entity) {
        return new Pet(
                entity.getId(),
                entity.getOwnerId(),
                entity.getName(),
                entity.getSpecies(),
                entity.getBreed(),
                entity.getBirthDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }
}