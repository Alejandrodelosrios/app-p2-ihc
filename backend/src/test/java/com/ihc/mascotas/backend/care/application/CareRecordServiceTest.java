package com.ihc.mascotas.backend.care.application;

import com.ihc.mascotas.backend.auth.domain.User;
import com.ihc.mascotas.backend.auth.domain.UserRepository;
import com.ihc.mascotas.backend.care.domain.CareHistoryAction;
import com.ihc.mascotas.backend.care.domain.CareHistoryEntry;
import com.ihc.mascotas.backend.care.domain.CareHistoryRepository;
import com.ihc.mascotas.backend.care.domain.CareRecord;
import com.ihc.mascotas.backend.care.domain.CareRecordRepository;
import com.ihc.mascotas.backend.care.domain.CareRecordStatus;
import com.ihc.mascotas.backend.pet.domain.Pet;
import com.ihc.mascotas.backend.pet.domain.PetRepository;
import com.ihc.mascotas.backend.pet.domain.PetSpecies;
import com.ihc.mascotas.backend.shared.exception.ConflictException;
import com.ihc.mascotas.backend.shared.exception.ForbiddenException;
import com.ihc.mascotas.backend.shared.exception.NotAllowedException;
import com.ihc.mascotas.backend.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CareRecordServiceTest {

    private static final UUID PET_ID = UUID.fromString("c3a1f5d2-7b84-4e19-a6c0-5d2e8f1b9a01");
    private static final UUID OTHER_PET_ID = UUID.fromString("d4b2a6e3-8c95-4f2a-b7d1-6e3f9a2c0b02");

    private final CareRecordRepository careRecords = mock(CareRecordRepository.class);
    private final CareHistoryRepository history = mock(CareHistoryRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final PetRepository pets = mock(PetRepository.class);
    private CareRecordService service;

    @BeforeEach
    void setUp() {
        reset(careRecords, history, users, pets);
        service = new CareRecordService(careRecords, history, users, pets);
    }

    // ---------------------------------------------------------------- crear

    @Test
    void createStartsAsPending() {
        User user = user();
        givenActivePet(pet(user.id(), "Milo"));
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(careRecords.save(any(CareRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CareRecord created = service
                .create(user.email(), PET_ID, "Vacuna", LocalDate.now().plusDays(1), null, null)
                .record();

        assertEquals(CareRecordStatus.PENDING, created.status());
    }

    @Test
    void createWithPastDateIsRejected() {
        User user = user();
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));

        assertThrows(
                NotAllowedException.class,
                () -> service.create(
                        user.email(),
                        PET_ID,
                        "Vacuna",
                        LocalDate.now().minusDays(1),
                        null,
                        null
                )
        );

        verify(careRecords, never()).save(any());
    }

    @Test
    void createStoresPetIdAndReturnsPetData() {
        User user = user();
        givenActivePet(pet(user.id(), "Milo"));
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(careRecords.save(any(CareRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CareRecordView view = service.create(
                user.email(), PET_ID, "Vacuna", LocalDate.now().plusDays(1), null, null);

        assertEquals(PET_ID, view.record().petId());
        assertEquals("Milo", view.petName());
        assertEquals(PetSpecies.DOG, view.petSpecies());
        assertTrue(view.petActive());
    }

    @Test
    void createWithNonExistentPetFailsAndDoesNotSave() {
        User user = user();
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(pets.findActiveByIdAndOwnerId(PET_ID, user.id())).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.create(user.email(), PET_ID, "Vacuna", LocalDate.now().plusDays(1), null, null)
        );

        verify(careRecords, never()).save(any());
    }

    @Test
    void createValidatesPetAgainstAuthenticatedUserNotAgainstClientData() {
        // La mascota pertenece a Ana; Carla intenta usarla con su petId.
        User ana = user();
        User carla = stranger();
        givenActivePet(pet(ana.id(), "Milo"));
        when(users.findByEmail(carla.email())).thenReturn(Optional.of(carla));

        assertThrows(
                NotFoundException.class,
                () -> service.create(carla.email(), PET_ID, "Vacuna", LocalDate.now().plusDays(1), null, null)
        );

        verify(pets).findActiveByIdAndOwnerId(PET_ID, carla.id());
        verify(careRecords, never()).save(any());
    }

    @Test
    void createWithSoftDeletedPetFails() {
        User user = user();
        // Una mascota desactivada no aparece en findActiveByIdAndOwnerId.
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(pets.findActiveByIdAndOwnerId(PET_ID, user.id())).thenReturn(Optional.empty());
        when(pets.findById(PET_ID)).thenReturn(Optional.of(deactivatedPet(user.id(), "Milo")));

        assertThrows(
                NotFoundException.class,
                () -> service.create(user.email(), PET_ID, "Vacuna", LocalDate.now().plusDays(1), null, null)
        );

        verify(careRecords, never()).save(any());
    }

    @Test
    void createKeepsOwnerCreatorAndAssigneeSeparate() {
        User owner = user();
        User assignee = assignee();
        Pet pet = pet(owner.id(), "Milo");
        givenActivePet(pet);
        when(users.findByEmail(owner.email())).thenReturn(Optional.of(owner));
        when(users.findByEmail(assignee.email())).thenReturn(Optional.of(assignee));
        when(careRecords.save(any(CareRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CareRecord created = service.create(
                owner.email(), PET_ID, "Vacuna", LocalDate.now().plusDays(1), assignee.email(), null
        ).record();

        assertEquals(owner.id(), created.userId());                 // creador
        assertEquals(assignee.id(), created.assignedToUserId());    // asignado
        assertEquals(owner.id(), pet.ownerId());                    // dueño: no cambia al asignar
        assertEquals(PET_ID, created.petId());
    }

    @Test
    void createRecurringCareStoresInterval() {
        User user = user();
        givenActivePet(pet(user.id(), "Firulais"));
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(careRecords.save(any(CareRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CareRecord created = service.create(
                user.email(),
                PET_ID,
                "Baño",
                LocalDate.now().plusDays(1),
                null,
                15
        ).record();

        assertEquals(15, created.recurrenceIntervalDays());
        assertEquals(CareRecordStatus.PENDING, created.status());
    }

    @Test
    void creatingRecurringCareWithZeroDaysIsRejected() {
        User user = user();
        givenActivePet(pet(user.id(), "Firulais"));
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));

        assertThrows(
                NotAllowedException.class,
                () -> service.create(user.email(), PET_ID, "Baño", LocalDate.now(), null, 0)
        );

        verify(careRecords, never()).save(any());
    }

    @Test
    void createResolvesOptionalAssigneeByEmail() {
        User owner = user();
        User assignee = assignee();
        givenActivePet(pet(owner.id(), "Milo"));
        when(users.findByEmail(owner.email())).thenReturn(Optional.of(owner));
        when(users.findByEmail(assignee.email())).thenReturn(Optional.of(assignee));
        when(careRecords.save(any(CareRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CareRecord created = service.create(
                owner.email(), PET_ID, "Vacuna", LocalDate.now().plusDays(1), assignee.email(), null
        ).record();

        assertEquals(assignee.id(), created.assignedToUserId());
    }

    @Test
    void createWithoutAssigneeLeavesItEmpty() {
        User owner = user();
        givenActivePet(pet(owner.id(), "Milo"));
        when(users.findByEmail(owner.email())).thenReturn(Optional.of(owner));
        when(careRecords.save(any(CareRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CareRecord withNull = service
                .create(owner.email(), PET_ID, "Vacuna", LocalDate.now().plusDays(1), null, null).record();
        CareRecord withBlank = service
                .create(owner.email(), PET_ID, "Vacuna", LocalDate.now().plusDays(1), "   ", null).record();

        assertNull(withNull.assignedToUserId());
        assertNull(withBlank.assignedToUserId());
    }

    @Test
    void createWithUnknownAssigneeFails() {
        User owner = user();
        givenActivePet(pet(owner.id(), "Milo"));
        when(users.findByEmail(owner.email())).thenReturn(Optional.of(owner));
        when(users.findByEmail("nadie@example.com")).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.create(owner.email(), PET_ID, "Vacuna",
                        LocalDate.now().plusDays(1), "nadie@example.com", null)
        );

        verify(careRecords, never()).save(any());
    }

    // ------------------------------------------------------------- completar

    @Test
    void completingRecurringCareSchedulesNextOccurrence() {
        User user = user();
        givenActivePet(pet(user.id(), "Firulais"));
        CareRecord pending = new CareRecord(
                UUID.randomUUID(),
                user.id(),
                PET_ID,
                "Baño",
                LocalDate.now().plusDays(2),
                Instant.now(),
                CareRecordStatus.PENDING,
                null,
                15
        );
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(careRecords.findById(pending.id())).thenReturn(Optional.of(pending));
        when(careRecords.save(any(CareRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CareRecord result = service.complete(user.email(), pending.id()).record();

        assertEquals(CareRecordStatus.PENDING, result.status());
        assertEquals(LocalDate.now().plusDays(15), result.careDate());
        assertEquals(15, result.recurrenceIntervalDays());
        assertEquals(PET_ID, result.petId());
        verify(history).save(any(CareHistoryEntry.class));
    }

    @Test
    void completingNonRecurringCareStillMarksItCompleted() {
        User user = user();
        givenActivePet(pet(user.id(), "Milo"));
        CareRecord pending = record(user.id(), CareRecordStatus.PENDING);
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(careRecords.findById(pending.id())).thenReturn(Optional.of(pending));
        when(careRecords.save(any(CareRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CareRecord result = service.complete(user.email(), pending.id()).record();

        assertEquals(CareRecordStatus.COMPLETED, result.status());
        assertEquals(pending.careDate(), result.careDate());
        assertNull(result.recurrenceIntervalDays());
    }

    @Test
    void pendingCareCanBeCompleted() {
        User user = user();
        givenActivePet(pet(user.id(), "Milo"));
        CareRecord pending = record(user.id(), CareRecordStatus.PENDING);
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(careRecords.findById(pending.id())).thenReturn(Optional.of(pending));
        when(careRecords.save(any(CareRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CareRecord completed = service.complete(user.email(), pending.id()).record();

        assertEquals(CareRecordStatus.COMPLETED, completed.status());
        verify(careRecords).save(completed);
    }

    @Test
    void assignedUserCanCompleteCareAndBeRecordedAsActor() {
        User owner = user();
        User assignee = assignee();
        Pet pet = pet(owner.id(), "Milo");
        CareRecord pending = record(owner.id(), assignee.id(), CareRecordStatus.PENDING);
        // El asignado NO es dueño: la mascota se lee sin filtro de dueño.
        when(pets.findById(PET_ID)).thenReturn(Optional.of(pet));
        when(users.findByEmail(assignee.email())).thenReturn(Optional.of(assignee));
        when(careRecords.findById(pending.id())).thenReturn(Optional.of(pending));
        when(careRecords.save(any(CareRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CareRecord completed = service.complete(assignee.email(), pending.id()).record();

        assertEquals(owner.id(), completed.userId());
        assertEquals(assignee.id(), completed.assignedToUserId());
        ArgumentCaptor<CareHistoryEntry> captor = ArgumentCaptor.forClass(CareHistoryEntry.class);
        verify(history).save(captor.capture());
        assertEquals(assignee.id(), captor.getValue().userId());
        verify(pets, never()).findActiveByIdAndOwnerId(any(), any());
    }

    @Test
    void completedCareCannotBeCompletedAgain() {
        User user = user();
        CareRecord completed = record(user.id(), CareRecordStatus.COMPLETED);
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(careRecords.findById(completed.id())).thenReturn(Optional.of(completed));

        assertThrows(ConflictException.class, () -> service.complete(user.email(), completed.id()));
        verify(careRecords, never()).save(any());
    }

    @Test
    void completingCareOnlyChangesStatus() {
        User user = user();
        givenActivePet(pet(user.id(), "Milo"));
        CareRecord pending = record(user.id(), CareRecordStatus.PENDING);
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(careRecords.findById(pending.id())).thenReturn(Optional.of(pending));
        when(careRecords.save(any(CareRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CareRecord completed = service.complete(user.email(), pending.id()).record();

        assertEquals(pending.id(), completed.id());
        assertEquals(pending.userId(), completed.userId());
        assertEquals(pending.petId(), completed.petId());
        assertEquals(pending.care(), completed.care());
        assertEquals(pending.careDate(), completed.careDate());
        assertEquals(pending.createdAt(), completed.createdAt());
        assertEquals(CareRecordStatus.COMPLETED, completed.status());
    }

    @Test
    void completingCareOfDeactivatedPetStillWorks() {
        User user = user();
        when(pets.findById(PET_ID)).thenReturn(Optional.of(deactivatedPet(user.id(), "Milo")));
        CareRecord pending = record(user.id(), CareRecordStatus.PENDING);
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(careRecords.findById(pending.id())).thenReturn(Optional.of(pending));
        when(careRecords.save(any(CareRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CareRecordView view = service.complete(user.email(), pending.id());

        assertEquals(CareRecordStatus.COMPLETED, view.record().status());
        assertFalse(view.petActive());
        assertEquals("Milo", view.petName());
    }

    @Test
    void strangerCannotCompleteCare() {
        User owner = user();
        User stranger = stranger();
        CareRecord pending = record(owner.id(), assignee().id(), CareRecordStatus.PENDING);
        when(users.findByEmail(stranger.email())).thenReturn(Optional.of(stranger));
        when(careRecords.findById(pending.id())).thenReturn(Optional.of(pending));

        assertThrows(ForbiddenException.class, () -> service.complete(stranger.email(), pending.id()));

        verify(careRecords, never()).save(any());
        verify(history, never()).save(any());
    }

    // ------------------------------------------------------------- historial

    @Test
    void completingCareCreatesHistoryEntryWithPetSnapshot() {
        User user = user();
        givenActivePet(pet(user.id(), "Milo"));
        CareRecord pending = record(user.id(), CareRecordStatus.PENDING);
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(careRecords.findById(pending.id())).thenReturn(Optional.of(pending));
        when(careRecords.save(any(CareRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.complete(user.email(), pending.id());

        ArgumentCaptor<CareHistoryEntry> captor = ArgumentCaptor.forClass(CareHistoryEntry.class);
        verify(history).save(captor.capture());
        CareHistoryEntry entry = captor.getValue();
        assertEquals(CareHistoryAction.COMPLETED, entry.action());
        assertEquals(pending.id(), entry.careRecordId());
        assertEquals(PET_ID, entry.petId());
        assertEquals("Milo", entry.petName());
        assertEquals(LocalDate.now(), entry.currentCareDate());
    }

    @Test
    void reschedulingCareCreatesHistoryEntry() {
        User user = user();
        givenActivePet(pet(user.id(), "Milo"));
        CareRecord pending = record(user.id(), CareRecordStatus.PENDING);
        LocalDate newDate = LocalDate.now().plusDays(2);
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(careRecords.findById(pending.id())).thenReturn(Optional.of(pending));
        when(careRecords.save(any(CareRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.update(
                user.email(),
                pending.id(),
                pending.petId(),
                pending.care(),
                newDate,
                null,
                null
        );

        ArgumentCaptor<CareHistoryEntry> captor = ArgumentCaptor.forClass(CareHistoryEntry.class);
        verify(history).save(captor.capture());
        CareHistoryEntry entry = captor.getValue();
        assertEquals(CareHistoryAction.RESCHEDULED, entry.action());
        assertEquals(pending.careDate(), entry.previousCareDate());
        assertEquals(newDate, entry.currentCareDate());
        assertEquals(PET_ID, entry.petId());
        assertEquals("Milo", entry.petName());
    }

    @Test
    void renamingPetDoesNotAlterAlreadyStoredHistoryButNewEventsUseCurrentName() {
        User user = user();
        CareRecord pending = record(user.id(), CareRecordStatus.PENDING);
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(careRecords.findById(pending.id())).thenReturn(Optional.of(pending));
        when(careRecords.save(any(CareRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Evento 1: reprogramar cuando la mascota se llama "Milo".
        givenActivePet(pet(user.id(), "Milo"));
        service.update(user.email(), pending.id(), PET_ID, pending.care(),
                LocalDate.now().plusDays(2), null, null);

        // La mascota se renombra.
        givenActivePet(pet(user.id(), "Milo Junior"));

        // Evento 2: reprogramar de nuevo.
        service.update(user.email(), pending.id(), PET_ID, pending.care(),
                LocalDate.now().plusDays(3), null, null);

        ArgumentCaptor<CareHistoryEntry> captor = ArgumentCaptor.forClass(CareHistoryEntry.class);
        verify(history, times(2)).save(captor.capture());
        List<CareHistoryEntry> entries = captor.getAllValues();
        assertEquals("Milo", entries.get(0).petName());          // snapshot previo intacto
        assertEquals("Milo Junior", entries.get(1).petName());   // evento nuevo con nombre vigente
    }

    @Test
    void completedCareCannotBeRescheduled() {
        User user = user();
        CareRecord completed = record(user.id(), CareRecordStatus.COMPLETED);

        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(careRecords.findById(completed.id())).thenReturn(Optional.of(completed));

        LocalDate newDate = LocalDate.now().plusDays(2);

        assertThrows(
                NotAllowedException.class,
                () -> service.update(
                        user.email(),
                        completed.id(),
                        completed.petId(),
                        completed.care(),
                        newDate,
                        null,
                        null
                )
        );

        verify(careRecords, never()).save(any());
    }

    @Test
    void pendingCareCannotBeRescheduledToPast() {
        User user = user();
        CareRecord pending = record(user.id(), CareRecordStatus.PENDING);
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(careRecords.findById(pending.id())).thenReturn(Optional.of(pending));

        LocalDate pastDate = LocalDate.now().minusDays(1);

        assertThrows(
                NotAllowedException.class,
                () -> service.update(
                        user.email(),
                        pending.id(),
                        pending.petId(),
                        pending.care(),
                        pastDate,
                        null,
                        null
                )
        );

        verify(careRecords, never()).save(any());
    }

    @Test
    void updateWithPastDateIsRejected() {
        User user = user();
        CareRecord pending = record(user.id(), CareRecordStatus.PENDING);
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(careRecords.findById(pending.id())).thenReturn(Optional.of(pending));

        assertThrows(
                NotAllowedException.class,
                () -> service.update(
                        user.email(),
                        pending.id(),
                        pending.petId(),
                        pending.care(),
                        LocalDate.now().minusDays(1),
                        null,
                        null
                )
        );

        verify(careRecords, never()).save(any());
    }

    // ---------------------------------------------------------------- editar

    @Test
    void updateKeepsIdentityAndStatus() {
        User user = user();
        givenActivePet(pet(user.id(), "Milo"));
        CareRecord pending = record(user.id(), CareRecordStatus.PENDING);
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(careRecords.findById(pending.id())).thenReturn(Optional.of(pending));
        when(careRecords.save(any(CareRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDate newDate = LocalDate.now().plusDays(1);

        CareRecord updated = service.update(
                user.email(),
                pending.id(),
                PET_ID,
                "Control",
                newDate,
                null,
                null
        ).record();

        assertEquals(pending.id(), updated.id());
        assertEquals(pending.userId(), updated.userId());
        assertEquals(pending.createdAt(), updated.createdAt());
        assertEquals(CareRecordStatus.PENDING, updated.status());
        assertEquals(PET_ID, updated.petId());
        assertEquals("Control", updated.care());
        assertEquals(newDate, updated.careDate());
    }

    @Test
    void updateCanMoveCareToAnotherOwnedPet() {
        User user = user();
        CareRecord pending = record(user.id(), CareRecordStatus.PENDING);
        Pet otherPet = new Pet(OTHER_PET_ID, user.id(), "Luna", PetSpecies.CAT,
                null, null, Instant.parse("2026-10-08T00:00:00Z"),
                Instant.parse("2026-10-08T00:00:00Z"), null);
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(careRecords.findById(pending.id())).thenReturn(Optional.of(pending));
        when(pets.findActiveByIdAndOwnerId(OTHER_PET_ID, user.id())).thenReturn(Optional.of(otherPet));
        when(careRecords.save(any(CareRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CareRecordView view = service.update(
                user.email(), pending.id(), OTHER_PET_ID, pending.care(), pending.careDate(), null, null);

        assertEquals(OTHER_PET_ID, view.record().petId());
        assertEquals("Luna", view.petName());
        assertEquals(user.id(), view.record().userId());   // el creador no cambia
    }

    @Test
    void updateWithPetNotOwnedByUserFails() {
        User user = user();
        CareRecord pending = record(user.id(), CareRecordStatus.PENDING);
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(careRecords.findById(pending.id())).thenReturn(Optional.of(pending));
        when(pets.findActiveByIdAndOwnerId(OTHER_PET_ID, user.id())).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.update(user.email(), pending.id(), OTHER_PET_ID,
                        pending.care(), pending.careDate(), null, null)
        );

        verify(careRecords, never()).save(any());
    }

    @Test
    void updateWithSamePetDoesNotRequireItToBeActive() {
        User user = user();
        CareRecord pending = record(user.id(), CareRecordStatus.PENDING);
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(careRecords.findById(pending.id())).thenReturn(Optional.of(pending));
        when(pets.findById(PET_ID)).thenReturn(Optional.of(deactivatedPet(user.id(), "Milo")));
        when(careRecords.save(any(CareRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CareRecordView view = service.update(
                user.email(), pending.id(), PET_ID, "Nota nueva", pending.careDate(), null, null);

        assertEquals("Nota nueva", view.record().care());
        assertFalse(view.petActive());
        verify(pets, never()).findActiveByIdAndOwnerId(any(), any());
    }

    @Test
    void completedCareCannotChangePet() {
        User user = user();
        CareRecord completed = record(user.id(), CareRecordStatus.COMPLETED);
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(careRecords.findById(completed.id())).thenReturn(Optional.of(completed));

        assertThrows(
                NotAllowedException.class,
                () -> service.update(user.email(), completed.id(), OTHER_PET_ID,
                        completed.care(), completed.careDate(), null, null)
        );

        verify(careRecords, never()).save(any());
    }

    @Test
    void assigneeCannotUpdateCare() {
        User owner = user();
        User assignee = assignee();
        CareRecord assigned = record(owner.id(), assignee.id(), CareRecordStatus.PENDING);
        when(users.findByEmail(assignee.email())).thenReturn(Optional.of(assignee));
        when(careRecords.findById(assigned.id())).thenReturn(Optional.of(assigned));

        assertThrows(
                NotFoundException.class,
                () -> service.update(assignee.email(), assigned.id(), PET_ID, "Control",
                        LocalDate.now().plusDays(3), null, null)
        );

        verify(careRecords, never()).save(any());
    }

    @Test
    void ownerCanChangeAndRemoveAssigneeOnUpdate() {
        User owner = user();
        User assignee = assignee();
        givenActivePet(pet(owner.id(), "Milo"));
        CareRecord pending = record(owner.id(), CareRecordStatus.PENDING);
        when(users.findByEmail(owner.email())).thenReturn(Optional.of(owner));
        when(users.findByEmail(assignee.email())).thenReturn(Optional.of(assignee));
        when(careRecords.findById(pending.id())).thenReturn(Optional.of(pending));
        when(careRecords.save(any(CareRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CareRecord assigned = service.update(owner.email(), pending.id(), PET_ID, pending.care(),
                pending.careDate(), assignee.email(), null).record();
        CareRecord cleared = service.update(owner.email(), pending.id(), PET_ID, pending.care(),
                pending.careDate(), null, null).record();

        assertEquals(assignee.id(), assigned.assignedToUserId());
        assertNull(cleared.assignedToUserId());
        // cambiar solo el responsable no es reprogramar: no hay historial
        verify(history, never()).save(any());
    }

    // ---------------------------------------------------------------- borrar

    @Test
    void deleteRemovesOwnedCare() {
        User user = user();
        CareRecord pending = record(user.id(), CareRecordStatus.PENDING);
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(careRecords.findById(pending.id())).thenReturn(Optional.of(pending));

        service.delete(user.email(), pending.id());

        verify(careRecords).deleteById(pending.id());
    }

    @Test
    void assigneeCannotDeleteCare() {
        User owner = user();
        User assignee = assignee();
        CareRecord assigned = record(owner.id(), assignee.id(), CareRecordStatus.PENDING);
        when(users.findByEmail(assignee.email())).thenReturn(Optional.of(assignee));
        when(careRecords.findById(assigned.id())).thenReturn(Optional.of(assigned));

        assertThrows(NotFoundException.class, () -> service.delete(assignee.email(), assigned.id()));

        verify(careRecords, never()).deleteById(any());
    }

    // --------------------------------------------------------------- listado

    @Test
    void filteredCareRecordsReturnMatchesWithPetData() {
        User user = user();
        CareRecord pending = record(user.id(), CareRecordStatus.PENDING);
        LocalDate fromDate = LocalDate.of(2026, 10, 1);
        LocalDate toDate = LocalDate.of(2026, 10, 31);

        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(careRecords.findByFilters(
                user.id(),
                PET_ID,
                CareRecordStatus.PENDING,
                "VACCINE",
                fromDate,
                toDate
        )).thenReturn(List.of(pending));
        when(pets.findAllByIds(Set.of(PET_ID))).thenReturn(List.of(pet(user.id(), "Milo")));

        List<CareRecordView> records = service.findByUserEmail(
                user.email(),
                PET_ID,
                CareRecordStatus.PENDING,
                "VACCINE",
                fromDate,
                toDate
        );

        assertEquals(1, records.size());
        assertEquals(pending, records.get(0).record());
        assertEquals("Milo", records.get(0).petName());
        assertEquals(PetSpecies.DOG, records.get(0).petSpecies());
    }

    @Test
    void filteredCareRecordsCanReturnEmptyList() {
        User user = user();

        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(careRecords.findByFilters(
                user.id(),
                PET_ID,
                CareRecordStatus.COMPLETED,
                "CONTROL",
                null,
                null
        )).thenReturn(List.of());

        List<CareRecordView> records = service.findByUserEmail(
                user.email(),
                PET_ID,
                CareRecordStatus.COMPLETED,
                "CONTROL",
                null,
                null
        );

        assertEquals(List.of(), records);
    }

    @Test
    void listingWithoutPetFilterPassesNullAndResolvesPetsInOneLookup() {
        User owner = user();
        User assignee = assignee();
        CareRecord assigned = record(owner.id(), assignee.id(), CareRecordStatus.PENDING);

        // El asignado ve el cuidado aunque no sea dueño de la mascota.
        when(users.findByEmail(assignee.email())).thenReturn(Optional.of(assignee));
        when(careRecords.findByFilters(assignee.id(), null, null, null, null, null))
                .thenReturn(List.of(assigned));
        when(pets.findAllByIds(Set.of(PET_ID))).thenReturn(List.of(pet(owner.id(), "Milo")));

        List<CareRecordView> records = service.findByUserEmail(
                assignee.email(), null, null, null, null, null);

        assertEquals(1, records.size());
        assertEquals("Milo", records.get(0).petName());
        verify(pets, times(1)).findAllByIds(any());
    }

    @Test
    void listingIncludesCareOfDeactivatedPetFlaggedAsInactive() {
        User user = user();
        CareRecord pending = record(user.id(), CareRecordStatus.PENDING);
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(careRecords.findByFilters(user.id(), null, null, null, null, null))
                .thenReturn(List.of(pending));
        when(pets.findAllByIds(Set.of(PET_ID))).thenReturn(List.of(deactivatedPet(user.id(), "Milo")));

        List<CareRecordView> records = service.findByUserEmail(
                user.email(), null, null, null, null, null);

        assertEquals(1, records.size());
        assertFalse(records.get(0).petActive());
        assertEquals("Milo", records.get(0).petName());
    }

    // --------------------------------------------------------------- helpers

    private void givenActivePet(Pet pet) {
        when(pets.findActiveByIdAndOwnerId(pet.id(), pet.ownerId())).thenReturn(Optional.of(pet));
        when(pets.findById(pet.id())).thenReturn(Optional.of(pet));
    }

    private static Pet pet(UUID ownerId, String name) {
        Instant now = Instant.parse("2026-10-08T00:00:00Z");
        return new Pet(PET_ID, ownerId, name, PetSpecies.DOG, null, null, now, now, null);
    }

    private static Pet deactivatedPet(UUID ownerId, String name) {
        Instant now = Instant.parse("2026-10-08T00:00:00Z");
        return new Pet(PET_ID, ownerId, name, PetSpecies.DOG, null, null, now, now, now);
    }

    private static User user() {
        Instant now = Instant.parse("2026-10-08T00:00:00Z");
        return new User(
                UUID.fromString("98e24f0f-9fdb-47a7-924d-ef8a7b753d3d"),
                "Ana",
                "ana@example.com",
                "hash",
                now,
                now
        );
    }

    private static User assignee() {
        Instant now = Instant.parse("2026-10-08T00:00:00Z");
        return new User(UUID.fromString("6f8b4e1f-0d31-4c7e-9af7-6a6d9d4c8f20"),
                "Luis", "luis@example.com", "hash", now, now);
    }

    private static User stranger() {
        Instant now = Instant.parse("2026-10-08T00:00:00Z");
        return new User(UUID.fromString("0d9c7f3e-8a12-4b55-b7c1-2e4a5f6b7c88"),
                "Carla", "carla@example.com", "hash", now, now);
    }

    private static CareRecord record(UUID userId, UUID assigneeId, CareRecordStatus status) {
        return new CareRecord(
                UUID.fromString("383d827e-ea91-4317-b0a9-a55768f759aa"),
                userId,
                PET_ID,
                "Vacuna",
                LocalDate.now().plusDays(1),
                Instant.parse("2026-10-08T00:00:00Z"),
                status,
                assigneeId,
                null
        );
    }

    private static CareRecord record(UUID userId, CareRecordStatus status) {
        return record(userId, null, status);
    }
}