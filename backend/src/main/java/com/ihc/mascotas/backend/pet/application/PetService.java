package com.ihc.mascotas.backend.pet.application;

import com.ihc.mascotas.backend.auth.domain.User;
import com.ihc.mascotas.backend.auth.domain.UserRepository;
import com.ihc.mascotas.backend.pet.domain.Pet;
import com.ihc.mascotas.backend.pet.domain.PetRepository;
import com.ihc.mascotas.backend.pet.domain.PetSpecies;
import com.ihc.mascotas.backend.shared.exception.NotFoundException;
import com.ihc.mascotas.backend.shared.exception.NotAllowedException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PetService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;

    public PetService(
            PetRepository petRepository,
            UserRepository userRepository
    ) {
        this.petRepository = petRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Pet create(
            String email,
            String name,
            PetSpecies species,
            String breed,
            LocalDate birthDate
    ) {
        User owner = findUserByEmail(email);

        validateBirthDate(birthDate);

        Instant now = Instant.now();

        Pet pet = new Pet(
                UUID.randomUUID(),
                owner.id(),
                name.trim(),
                species,
                normalizeBreed(breed),
                birthDate,
                now,
                now,
                null
        );

        return petRepository.save(pet);
    }

    @Transactional(readOnly = true)
    public List<Pet> findAllByOwner(String email) {
        User owner = findUserByEmail(email);

        return petRepository.findAllActiveByOwnerId(owner.id());
    }

    @Transactional(readOnly = true)
    public Pet findById(String email, UUID petId) {
        User owner = findUserByEmail(email);

        return findOwnedActivePet(petId, owner.id());
    }

    @Transactional
    public Pet update(
            String email,
            UUID petId,
            String name,
            PetSpecies species,
            String breed,
            LocalDate birthDate
    ) {
        User owner = findUserByEmail(email);

        Pet current = findOwnedActivePet(petId, owner.id());

        validateBirthDate(birthDate);

        Pet updated = new Pet(
                current.id(),
                current.ownerId(),
                name.trim(),
                species,
                normalizeBreed(breed),
                birthDate,
                current.createdAt(),
                Instant.now(),
                null
        );

        return petRepository.save(updated);
    }

    @Transactional
    public void delete(String email, UUID petId) {
        User owner = findUserByEmail(email);

        Pet current = findOwnedActivePet(petId, owner.id());

        Pet deleted = new Pet(
                current.id(),
                current.ownerId(),
                current.name(),
                current.species(),
                current.breed(),
                current.birthDate(),
                current.createdAt(),
                Instant.now(),
                Instant.now()
        );

        petRepository.save(deleted);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new NotFoundException("Usuario no encontrado")
                );
    }

    private Pet findOwnedActivePet(UUID petId, UUID ownerId) {
        return petRepository.findActiveByIdAndOwnerId(
                petId,
                ownerId
        ).orElseThrow(() ->
                new NotFoundException("Mascota no encontrada")
        );
    }

    private void validateBirthDate(LocalDate birthDate) {
        if (birthDate != null && birthDate.isAfter(LocalDate.now())) {
            throw new NotAllowedException(
                    "La fecha de nacimiento no puede ser futura"
            );
        }
    }

    private String normalizeBreed(String breed) {
        if (breed == null || breed.isBlank()) {
            return null;
        }

        return breed.trim();
    }
}