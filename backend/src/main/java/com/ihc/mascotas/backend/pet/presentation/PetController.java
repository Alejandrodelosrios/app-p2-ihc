package com.ihc.mascotas.backend.pet.presentation;

import com.ihc.mascotas.backend.pet.application.PetService;
import com.ihc.mascotas.backend.pet.domain.Pet;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PetResponse create(
            Principal principal,
            @Valid @RequestBody CreatePetRequest request
    ) {
        Pet pet = petService.create(
                principal.getName(),
                request.name(),
                request.species(),
                request.breed(),
                request.birthDate()
        );

        return toResponse(pet);
    }

    @GetMapping
    public List<PetResponse> findAll(Principal principal) {
        return petService.findAllByOwner(principal.getName())
                .stream()
                .map(PetController::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public PetResponse findById(
            Principal principal,
            @PathVariable UUID id
    ) {
        return toResponse(
                petService.findById(principal.getName(), id)
        );
    }

    @PutMapping("/{id}")
    public PetResponse update(
            Principal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePetRequest request
    ) {
        Pet pet = petService.update(
                principal.getName(),
                id,
                request.name(),
                request.species(),
                request.breed(),
                request.birthDate()
        );

        return toResponse(pet);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            Principal principal,
            @PathVariable UUID id
    ) {
        petService.delete(principal.getName(), id);
    }

    private static PetResponse toResponse(Pet pet) {
        return new PetResponse(
                pet.id(),
                pet.ownerId(),
                pet.name(),
                pet.species(),
                pet.breed(),
                pet.birthDate(),
                pet.createdAt(),
                pet.updatedAt()
        );
    }
}