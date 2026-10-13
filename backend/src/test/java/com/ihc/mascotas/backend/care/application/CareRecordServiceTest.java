package com.ihc.mascotas.backend.care.application;

import com.ihc.mascotas.backend.auth.domain.User;
import com.ihc.mascotas.backend.auth.domain.UserRepository;
import com.ihc.mascotas.backend.care.domain.CareRecord;
import com.ihc.mascotas.backend.care.domain.CareRecordRepository;
import com.ihc.mascotas.backend.care.domain.CareRecordStatus;
import com.ihc.mascotas.backend.shared.exception.ConflictException;
import com.ihc.mascotas.backend.shared.exception.NotAllowedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CareRecordServiceTest {

    private final CareRecordRepository careRecords = mock(CareRecordRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private CareRecordService service;

    @BeforeEach
    void setUp() {
        reset(careRecords, users);
        service = new CareRecordService(careRecords, users);
    }

    @Test
    void createStartsAsPending() {
        User user = user();
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(careRecords.save(any(CareRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CareRecord created = service.create(user.email(), "Milo", "Vacuna", "Perro", LocalDate.of(2026, 10, 8));

        assertEquals(CareRecordStatus.PENDING, created.status());
    }

    @Test
    void pendingCareCanBeCompleted() {
        User user = user();
        CareRecord pending = record(user.id(), CareRecordStatus.PENDING);
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(careRecords.findById(pending.id())).thenReturn(Optional.of(pending));
        when(careRecords.save(any(CareRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CareRecord completed = service.complete(user.email(), pending.id());

        assertEquals(CareRecordStatus.COMPLETED, completed.status());
        verify(careRecords).save(completed);
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
        CareRecord pending = record(user.id(), CareRecordStatus.PENDING);
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(careRecords.findById(pending.id())).thenReturn(Optional.of(pending));
        when(careRecords.save(any(CareRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CareRecord completed = service.complete(user.email(), pending.id());

        assertEquals(pending.id(), completed.id());
        assertEquals(pending.userId(), completed.userId());
        assertEquals(pending.petName(), completed.petName());
        assertEquals(pending.care(), completed.care());
        assertEquals(pending.animalType(), completed.animalType());
        assertEquals(pending.careDate(), completed.careDate());
        assertEquals(pending.createdAt(), completed.createdAt());
        assertEquals(CareRecordStatus.COMPLETED, completed.status());
    }

    @Test
    void updateKeepsIdentityAndStatus() {
        User user = user();
        CareRecord pending = record(user.id(), CareRecordStatus.PENDING);
        when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(careRecords.findById(pending.id())).thenReturn(Optional.of(pending));
        when(careRecords.save(any(CareRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDate newDate = LocalDate.now().plusDays(1);

        CareRecord updated = service.update(
                user.email(),
                pending.id(),
                "Milo actualizado",
                "Control",
                "Perro",
                newDate
        );

        assertEquals(pending.id(), updated.id());
        assertEquals(pending.userId(), updated.userId());
        assertEquals(pending.createdAt(), updated.createdAt());
        assertEquals(CareRecordStatus.PENDING, updated.status());
        assertEquals("Milo actualizado", updated.petName());
        assertEquals("Control", updated.care());
        assertEquals(newDate, updated.careDate());
    }

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
    void completedCareCannotBeRescheduled() {
        User user = user();
        CareRecord completed = record(user.id(), CareRecordStatus.COMPLETED);

        when(users.findByEmail(user.email()))
                .thenReturn(Optional.of(user));

        when(careRecords.findById(completed.id()))
                .thenReturn(Optional.of(completed));

        LocalDate newDate = LocalDate.now().plusDays(1);

        assertThrows(
                NotAllowedException.class,
                () -> service.update(
                        user.email(),
                        completed.id(),
                        completed.petName(),
                        completed.care(),
                        completed.animalType(),
                        newDate
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
                        pending.petName(),
                        pending.care(),
                        pending.animalType(),
                        pastDate
                )
        );

        verify(careRecords, never()).save(any());
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

    private static CareRecord record(UUID userId, CareRecordStatus status) {
        return new CareRecord(
                UUID.fromString("383d827e-ea91-4317-b0a9-a55768f759aa"),
                userId,
                "Milo",
                "Vacuna",
                "Perro",
                LocalDate.of(2026, 10, 8),
                Instant.parse("2026-10-08T00:00:00Z"),
                status
        );
    }
}
