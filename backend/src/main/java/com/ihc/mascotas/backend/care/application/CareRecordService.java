package com.ihc.mascotas.backend.care.application;

import com.ihc.mascotas.backend.auth.domain.User;
import com.ihc.mascotas.backend.auth.domain.UserRepository;
import com.ihc.mascotas.backend.care.domain.CareHistoryAction;
import com.ihc.mascotas.backend.care.domain.CareHistoryEntry;
import com.ihc.mascotas.backend.care.domain.CareHistoryRepository;
import com.ihc.mascotas.backend.care.domain.CareRecord;
import com.ihc.mascotas.backend.care.domain.CareRecordRepository;
import com.ihc.mascotas.backend.care.domain.CareRecordStatus;
import com.ihc.mascotas.backend.shared.exception.ConflictException;
import com.ihc.mascotas.backend.shared.exception.ForbiddenException;
import com.ihc.mascotas.backend.shared.exception.NotAllowedException;
import com.ihc.mascotas.backend.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.Locale;

@Service
public class CareRecordService {
    private final CareRecordRepository careRecordRepository;
    private final CareHistoryRepository careHistoryRepository;
    private final UserRepository userRepository;

    public CareRecordService(
            CareRecordRepository careRecordRepository,
            CareHistoryRepository careHistoryRepository,
            UserRepository userRepository
    ) {
        this.careRecordRepository = careRecordRepository;
        this.careHistoryRepository = careHistoryRepository;
        this.userRepository = userRepository;
    }

    public CareRecord create(
            String email,
            String petName,
            String care,
            String animalType,
            LocalDate careDate,
            String assigneeEmail,
            Integer recurrenceIntervalDays
    ) {
        validateRecurrenceInterval(recurrenceIntervalDays);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UUID assigneeId = resolveAssigneeId(assigneeEmail);
        CareRecord record = new CareRecord(
                UUID.randomUUID(),
                user.id(),
                petName,
                care,
                animalType,
                careDate,
                Instant.now(),
                CareRecordStatus.PENDING,
                assigneeId,
                recurrenceIntervalDays
        );

        return careRecordRepository.save(record);
    }

    public CareRecord create(
            String email,
            String petName,
            String care,
            String animalType,
            LocalDate careDate
    ) {
        return create(email, petName, care, animalType, careDate, null, null);
    }

    public CareRecord create(
            String email,
            String petName,
            String care,
            String animalType,
            LocalDate careDate,
            String assigneeEmail
    ) {
        return create(email, petName, care, animalType, careDate, assigneeEmail, null);
    }

    public List<CareRecord> findByUserEmail(String email) {
        return findByUserEmail(email, null, null, null, null, null);
    }

    public List<CareRecord> findByUserEmail(
            String email,
            String petName,
            CareRecordStatus status,
            String careType,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return careRecordRepository.findByFilters(
                user.id(),
                petName,
                status,
                careType,
                fromDate,
                toDate
        );
    }

    @Transactional
    public CareRecord complete(String email, UUID id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        CareRecord record = careRecordRepository.findById(id)
                .filter(careRecord -> isOwnerOrAssignee(careRecord, user.id()))
                .orElseThrow(() -> new ForbiddenException("No tienes permiso para completar este cuidado"));

        if (record.status() == CareRecordStatus.COMPLETED) {
            throw new ConflictException("El cuidado ya fue realizado");
        }

        LocalDate completionDate = LocalDate.now();
        boolean recurring = record.recurrenceIntervalDays() != null;
        LocalDate nextCareDate = recurring
                ? completionDate.plusDays(record.recurrenceIntervalDays())
                : record.careDate();
        CareRecordStatus nextStatus = recurring
                ? CareRecordStatus.PENDING
                : CareRecordStatus.COMPLETED;

        CareRecord updated = new CareRecord(
                record.id(),
                record.userId(),
                record.petName(),
                record.care(),
                record.animalType(),
                nextCareDate,
                record.createdAt(),
                nextStatus,
                record.assignedToUserId(),
                record.recurrenceIntervalDays()
        );

        CareRecord saved = careRecordRepository.save(updated);
        careHistoryRepository.save(new CareHistoryEntry(
                UUID.randomUUID(),
                user.id(),
                record.id(),
                record.petName(),
                record.care(),
                CareHistoryAction.COMPLETED,
                null,
                completionDate,
                Instant.now()
        ));
        return saved;
    }

    @Transactional
    public CareRecord update(
            String email,
            UUID id,
            String petName,
            String care,
            String animalType,
            LocalDate careDate,
            String assigneeEmail,
            Integer recurrenceIntervalDays
    ) {
        validateRecurrenceInterval(recurrenceIntervalDays);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        CareRecord record = careRecordRepository.findById(id)
                .filter(careRecord -> careRecord.userId().equals(user.id()))
                .orElseThrow(() -> new NotFoundException("Cuidado no encontrado"));

        boolean dateChanged = !record.careDate().equals(careDate);
        boolean recurrenceChanged = !Objects.equals(
                record.recurrenceIntervalDays(),
                recurrenceIntervalDays
        );

        if (record.status() == CareRecordStatus.COMPLETED && dateChanged) {
            throw new NotAllowedException("No puedes reprogramar un cuidado ya hecho");
        }

        if (record.status() == CareRecordStatus.COMPLETED && recurrenceChanged) {
            throw new NotAllowedException(
                    "No puedes cambiar la recurrencia de un cuidado ya realizado"
            );
        }

        if (dateChanged && careDate.isBefore(LocalDate.now())) {
            throw new NotAllowedException("La nueva fecha no puede ser anterior a hoy");
        }

        UUID assigneeId = resolveAssigneeId(assigneeEmail);

        CareRecord updated = new CareRecord(
                record.id(),
                user.id(),
                petName,
                care,
                animalType,
                careDate,
                record.createdAt(),
                record.status(),
                assigneeId,
                recurrenceIntervalDays
        );

        CareRecord saved = careRecordRepository.save(updated);

        if (dateChanged) {
            careHistoryRepository.save(new CareHistoryEntry(
                    UUID.randomUUID(),
                    user.id(),
                    record.id(),
                    updated.petName(),
                    updated.care(),
                    CareHistoryAction.RESCHEDULED,
                    record.careDate(),
                    updated.careDate(),
                    Instant.now()
            ));
        }

        return saved;
    }

    public CareRecord update(
            String email,
            UUID id,
            String petName,
            String care,
            String animalType,
            LocalDate careDate
    ) {
        return update(email, id, petName, care, animalType, careDate, null, null);
    }

    public CareRecord update(
            String email,
            UUID id,
            String petName,
            String care,
            String animalType,
            LocalDate careDate,
            String assigneeEmail
    ) {
        return update(email, id, petName, care, animalType, careDate, assigneeEmail, null);
    }

    public void delete(String email, UUID id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        CareRecord record = careRecordRepository.findById(id)
                .filter(careRecord -> careRecord.userId().equals(user.id()))
                .orElseThrow(() -> new NotFoundException("Cuidado no encontrado"));

        careRecordRepository.deleteById(record.id());
    }

    private UUID resolveAssigneeId(String assigneeEmail) {
        if (assigneeEmail == null || assigneeEmail.isBlank()) {
            return null;
        }

        return userRepository.findByEmail(assigneeEmail.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new NotFoundException("Usuario asignado no encontrado"))
                .id();
    }

    private boolean isOwnerOrAssignee(CareRecord record, UUID userId) {
        return record.userId().equals(userId)
                || Objects.equals(record.assignedToUserId(), userId);
    }

    private void validateRecurrenceInterval(Integer interval) {
        if (interval != null && interval < 1) {
            throw new NotAllowedException(
                    "El intervalo de recurrencia debe ser de al menos un día"
            );
        }
    }
}
