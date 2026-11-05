package com.ihc.mascotas.backend.pet.application;

import com.ihc.mascotas.backend.auth.domain.User;
import com.ihc.mascotas.backend.auth.domain.UserRepository;
import com.ihc.mascotas.backend.pet.domain.Pet;
import com.ihc.mascotas.backend.pet.domain.PetRepository;
import com.ihc.mascotas.backend.pet.domain.PetSpecies;
import com.ihc.mascotas.backend.shared.exception.NotAllowedException;
import com.ihc.mascotas.backend.shared.exception.NotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PetServiceTest {

    private final PetRepository pets = mock(PetRepository.class);
    private final UserRepository users = mock(UserRepository.class);

    private PetService service;

    @BeforeEach
    void setUp() {
        service = new PetService(pets, users);
    }

    private User user() {
        Instant now = Instant.now();

        return new User(
                UUID.randomUUID(),
                "Alejandro",
                "alejandro@example.com",
                "hash",
                now,
                now
        );
    }

    @Test
    void createPetWithValidData() {
        User owner = user();

        when(users.findByEmail(owner.email()))
                .thenReturn(Optional.of(owner));

        when(pets.save(any(Pet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Pet created = service.create(
                owner.email(),
                "Firulais",
                PetSpecies.DOG,
                "Labrador",
                LocalDate.of(2022, 5, 10)
        );

        assertEquals("Firulais", created.name());
        assertEquals(PetSpecies.DOG, created.species());
        assertEquals(owner.id(), created.ownerId());
        assertTrue(created.isActive());
    }

    @Test
    void cannotCreatePetWithFutureBirthDate() {
        User owner = user();

        when(users.findByEmail(owner.email()))
                .thenReturn(Optional.of(owner));

        assertThrows(
                NotAllowedException.class,
                () -> service.create(
                        owner.email(),
                        "Firulais",
                        PetSpecies.DOG,
                        null,
                        LocalDate.now().plusDays(1)
                )
        );

        verify(pets, never()).save(any());
    }

    @Test
    void cannotAccessPetOwnedByAnotherUser() {
        User owner = user();

        UUID petId = UUID.randomUUID();

        when(users.findByEmail(owner.email()))
                .thenReturn(Optional.of(owner));

        when(pets.findActiveByIdAndOwnerId(petId, owner.id()))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.findById(owner.email(), petId)
        );
    }

    @Test
    void deletePetUsesSoftDelete() {
        User owner = user();

        Pet pet = new Pet(
                UUID.randomUUID(),
                owner.id(),
                "Firulais",
                PetSpecies.DOG,
                "Labrador",
                null,
                Instant.now(),
                Instant.now(),
                null
        );

        when(users.findByEmail(owner.email()))
                .thenReturn(Optional.of(owner));

        when(pets.findActiveByIdAndOwnerId(pet.id(), owner.id()))
                .thenReturn(Optional.of(pet));

        when(pets.save(any(Pet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.delete(owner.email(), pet.id());

        verify(pets).save(argThat(saved ->
                saved.id().equals(pet.id())
                        && saved.deletedAt() != null
                        && saved.ownerId().equals(owner.id())
        ));
    }
}