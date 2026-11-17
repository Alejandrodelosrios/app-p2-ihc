package com.ihc.mascotas.backend.account.application;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ihc.mascotas.backend.analytics.application.AnalyticsService;
import com.ihc.mascotas.backend.analytics.domain.AnalyticsEvent;
import com.ihc.mascotas.backend.auth.domain.PasswordResetTokenRepository;
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
import com.ihc.mascotas.backend.shared.exception.NotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-15T12:00:00Z");

    private final UserRepository users = mock(UserRepository.class);
    private final PetRepository pets = mock(PetRepository.class);
    private final CareRecordRepository careRecords = mock(CareRecordRepository.class);
    private final CareHistoryRepository history = mock(CareHistoryRepository.class);
    private final PasswordResetTokenRepository resetTokens = mock(PasswordResetTokenRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final AnalyticsService analytics = mock(AnalyticsService.class);

    private AccountService service;
    private User me;
    private User other;

    @BeforeEach
    void setUp() {
        service = new AccountService(users, pets, careRecords, history, resetTokens, encoder, analytics,
                Clock.fixed(NOW, ZoneOffset.UTC));
        me = new User(UUID.randomUUID(), "Ana Pérez", "ana@example.com", "hash", NOW, NOW);
        other = new User(UUID.randomUUID(), "Luis Gómez", "luis@example.com", "hash", NOW, NOW);

        when(users.findByEmail(me.email())).thenReturn(Optional.of(me));
        when(encoder.encode(any())).thenReturn("random-hash");
        when(pets.findAllActiveByOwnerId(me.id())).thenReturn(List.of());
        when(history.findByUserIdOrderByOccurredAtDesc(me.id())).thenReturn(List.of());
        when(careRecords.findByFilters(any(), any(), any(), any(), any(), any())).thenReturn(List.of());
    }

    private CareRecord care(UUID creator, UUID assignee, CareRecordStatus status) {
        return new CareRecord(UUID.randomUUID(), creator, UUID.randomUUID(), "Vacuna",
                LocalDate.of(2026, 12, 1), NOW, status, assignee, null);
    }

    private void relatedCares(CareRecord... records) {
        when(careRecords.findByFilters(eq(me.id()), any(), any(), any(), any(), any()))
                .thenReturn(List.of(records));
    }

    @Test
    void deleteAccountAnonymizesTheUserAndRemovesPersonalData() {
        service.deleteAccount(me.email());

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        User anonymized = saved.getValue();

        assertEquals(me.id(), anonymized.id());
        assertEquals("Ana Pérez", anonymized.fullName());
        assertFalse(anonymized.email().contains("ana"));
        assertTrue(anonymized.email().startsWith("eliminado-"));
        assertNotNull(anonymized.deletedAt());
        assertFalse(anonymized.isActive());
        assertEquals("random-hash", anonymized.passwordHash());
        verify(resetTokens).deleteByUserId(me.id());
    }

    @Test
    void careAssignedToMeLosesTheAssignmentButStaysWithItsCreator() {
        CareRecord assigned = care(other.id(), me.id(), CareRecordStatus.PENDING);
        relatedCares(assigned);

        service.deleteAccount(me.email());

        ArgumentCaptor<CareRecord> saved = ArgumentCaptor.forClass(CareRecord.class);
        verify(careRecords).save(saved.capture());
        assertEquals(assigned.id(), saved.getValue().id());
        assertEquals(other.id(), saved.getValue().userId());
        assertNull(saved.getValue().assignedToUserId());
        verify(careRecords, never()).deleteById(any());
    }

    @Test
    void careIAssignedToSomeoneElseIsKeptUntouched() {
        relatedCares(care(me.id(), other.id(), CareRecordStatus.PENDING));

        service.deleteAccount(me.email());

        verify(careRecords, never()).save(any());
        verify(careRecords, never()).deleteById(any());
    }

    @Test
    void pendingCareThatNobodyElseSeesIsDeleted() {
        CareRecord alone = care(me.id(), null, CareRecordStatus.PENDING);
        relatedCares(alone);

        service.deleteAccount(me.email());

        verify(careRecords).deleteById(alone.id());
    }

    @Test
    void finishedCaresAreKeptAsHistory() {
        relatedCares(
                care(me.id(), null, CareRecordStatus.COMPLETED),
                care(other.id(), me.id(), CareRecordStatus.COMPLETED_LATE));

        service.deleteAccount(me.email());

        verify(careRecords, never()).save(any());
        verify(careRecords, never()).deleteById(any());
    }

    @Test
    void myPetsAreDeactivated() {
        Pet pet = new Pet(UUID.randomUUID(), me.id(), "Luna", PetSpecies.CAT, null, null, NOW, NOW, null);
        when(pets.findAllActiveByOwnerId(me.id())).thenReturn(List.of(pet));

        service.deleteAccount(me.email());

        ArgumentCaptor<Pet> saved = ArgumentCaptor.forClass(Pet.class);
        verify(pets).save(saved.capture());
        assertEquals(pet.id(), saved.getValue().id());
        assertFalse(saved.getValue().isActive());
    }

    @Test
    void successEventIsEmittedWithoutPersonalData() {
        service.deleteAccount(me.email());

        verify(analytics).track(eq(AnalyticsEvent.ACCOUNT_DELETE_SUCCEEDED), eq(java.util.Map.of()), isNull());
    }

    @Test
    void summaryDescribesWhatWouldHappenWithRealCounts() {
        Pet pet = new Pet(UUID.randomUUID(), me.id(), "Luna", PetSpecies.CAT, null, null, NOW, NOW, null);
        when(pets.findAllActiveByOwnerId(me.id())).thenReturn(List.of(pet, pet));
        relatedCares(
                care(other.id(), me.id(), CareRecordStatus.PENDING),
                care(other.id(), me.id(), CareRecordStatus.RESCHEDULED),
                care(me.id(), other.id(), CareRecordStatus.PENDING),
                care(me.id(), null, CareRecordStatus.PENDING),
                care(me.id(), null, CareRecordStatus.COMPLETED));
        when(history.findByUserIdOrderByOccurredAtDesc(me.id())).thenReturn(List.of(
                new CareHistoryEntry(UUID.randomUUID(), me.id(), UUID.randomUUID(), pet.id(), "Luna", "Vacuna",
                        CareHistoryAction.COMPLETED, null, LocalDate.of(2026, 11, 1), NOW, null)));

        DeletionSummary summary = service.deletionSummary(me.email());

        assertEquals(2, summary.activePets());
        assertEquals(2, summary.assignedToMePending());
        assertEquals(1, summary.sharedWithOthersPending());
        assertEquals(1, summary.unsharedPendingDeleted());
        assertEquals(1, summary.historyEntries());
        verify(users, never()).save(any());
    }

    @Test
    void exportOnlyContainsMyOwnDataAndMarksMyRole() {
        CareRecord assigned = care(other.id(), me.id(), CareRecordStatus.PENDING);
        relatedCares(assigned);
        Pet pet = new Pet(assigned.petId(), other.id(), "Rex", PetSpecies.DOG, null, null, NOW, NOW, null);
        when(pets.findAllByIds(any())).thenReturn(List.of(pet));

        AccountData data = service.exportData(me.email());

        assertEquals("ana@example.com", data.profile().email());
        assertEquals(1, data.cares().size());
        assertEquals("asignado", data.cares().getFirst().role());
        assertEquals("Rex", data.cares().getFirst().pet());
        assertFalse(data.toString().contains("luis@example.com"));
    }

    @Test
    void aDeletedAccountCannotBeUsedAgain() {
        User deleted = new User(me.id(), "Ana Pérez", "eliminado-x@eliminado.invalid", "hash",
                NOW, NOW, NOW);
        when(users.findByEmail("eliminado-x@eliminado.invalid")).thenReturn(Optional.of(deleted));

        assertThrows(NotFoundException.class, () -> service.deleteAccount("eliminado-x@eliminado.invalid"));
        assertThrows(NotFoundException.class, () -> service.exportData("eliminado-x@eliminado.invalid"));
    }
}