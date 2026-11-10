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
import com.ihc.mascotas.backend.shared.exception.ConflictException;
import com.ihc.mascotas.backend.shared.exception.ForbiddenException;
import com.ihc.mascotas.backend.shared.exception.NotAllowedException;
import com.ihc.mascotas.backend.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CareRecordService {
    private final CareRecordRepository careRecordRepository;
    private final CareHistoryRepository careHistoryRepository;
    private final UserRepository userRepository;
    private final PetRepository petRepository;

    public CareRecordService(
            CareRecordRepository careRecordRepository,
            CareHistoryRepository careHistoryRepository,
            UserRepository userRepository,
            PetRepository petRepository
    ) {
        this.careRecordRepository = careRecordRepository;
        this.careHistoryRepository = careHistoryRepository;
        this.userRepository = userRepository;
        this.petRepository = petRepository;
    }

    @Transactional
    public CareRecordView create(
            String email,
            UUID petId,
            String care,
            LocalDate careDate,
            String assigneeEmail,
            Integer recurrenceIntervalDays
    ) {
        validateCareDate(careDate);
        validateRecurrenceInterval(recurrenceIntervalDays);
        User user = findUser(email);

        // El dueño sale del JWT; el petId del cliente nunca se confía por sí solo.
        Pet pet = findActiveOwnedPet(petId, user.id());

        UUID assigneeId = resolveAssigneeId(assigneeEmail);
        CareRecord record = new CareRecord(
                UUID.randomUUID(),
                user.id(),          // creador/gestor (no es necesariamente el dueño)
                pet.id(),
                care,
                careDate,
                Instant.now(),
                CareRecordStatus.PENDING,
                assigneeId,
                recurrenceIntervalDays,
                careDate,
                recurrenceIntervalDays == null ? null : UUID.randomUUID(),
                null
        );

        return toView(careRecordRepository.save(record), pet);
    }

    @Transactional(readOnly = true)
    public List<CareRecordView> findByUserEmail(
            String email,
            UUID petId,
            CareRecordStatus status,
            String careType,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        User user = findUser(email);

        List<CareRecord> records = careRecordRepository.findByFilters(
                user.id(),
                petId,
                status,
                careType,
                fromDate,
                toDate
        );

        // Una sola consulta para todas las mascotas (incluye desactivadas).
        Set<UUID> petIds = records.stream()
                .map(CareRecord::petId)
                .collect(Collectors.toSet());
        Map<UUID, Pet> petsById = petRepository.findAllByIds(petIds).stream()
                .collect(Collectors.toMap(Pet::id, Function.identity()));

        return records.stream()
                .map(record -> toView(record, petsById.get(record.petId())))
                .toList();
    }

    @Transactional
    public CareRecordView complete(String email, UUID id) {
        User user = findUser(email);

        CareRecord record = careRecordRepository.findById(id)
                .filter(careRecord -> isOwnerOrAssignee(careRecord, user.id()))
                .orElseThrow(() -> new ForbiddenException("No tienes permiso para completar este cuidado"));

        if (record.status() == CareRecordStatus.COMPLETED) {
            throw new ConflictException("El cuidado ya fue realizado");
        }
        if (record.status() == CareRecordStatus.CANCELLED
                || record.careDate().isBefore(LocalDate.now())) {
            throw new NotAllowedException("Past or cancelled care records cannot be modified");
        }

        // Sin filtro de dueño ni de activa: el asignado no es dueño y la mascota puede estar desactivada.
        Pet pet = findPetIncludingInactive(record.petId());

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
                record.petId(),
                record.care(),
                nextCareDate,
                record.createdAt(),
                nextStatus,
                record.assignedToUserId(),
                record.recurrenceIntervalDays(),
                record.originalCareDate(),
                record.seriesId(),
                record.changeReason()
        );

        CareRecord saved = careRecordRepository.save(updated);
        careHistoryRepository.save(new CareHistoryEntry(
                UUID.randomUUID(),
                user.id(),
                record.id(),
                pet.id(),
                pet.name(),          // snapshot: nombre de la mascota al momento del evento
                record.care(),
                CareHistoryAction.COMPLETED,
                null,
                completionDate,
                Instant.now(),
                null
        ));
        return toView(saved, pet);
    }

    @Transactional
    public CareRecordView update(
            String email,
            UUID id,
            UUID petId,
            String care,
            LocalDate careDate,
            String assigneeEmail,
            Integer recurrenceIntervalDays
    ) {
        validateCareDate(careDate);
        validateRecurrenceInterval(recurrenceIntervalDays);
        User user = findUser(email);

        CareRecord record = careRecordRepository.findById(id)
                .filter(careRecord -> careRecord.userId().equals(user.id()))
                .orElseThrow(() -> new NotFoundException("Cuidado no encontrado"));

        boolean petChanged = !record.petId().equals(petId);
        boolean dateChanged = !Objects.equals(record.careDate(), careDate);
        boolean recurrenceChanged = !Objects.equals(
                record.recurrenceIntervalDays(),
                recurrenceIntervalDays
        );

        if (record.careDate().isBefore(LocalDate.now())
                && (dateChanged || petChanged || recurrenceChanged)) {
            throw new NotAllowedException("Past care records cannot be modified");
        }

        if (record.status() == CareRecordStatus.COMPLETED && dateChanged) {
            throw new NotAllowedException("No puedes reprogramar un cuidado ya hecho");
        }
        if (dateChanged) {
            throw new NotAllowedException(
                    "Date changes require the reschedule endpoint and a reason"
            );
        }

        if (record.status() == CareRecordStatus.COMPLETED && recurrenceChanged) {
            throw new NotAllowedException(
                    "No puedes cambiar la recurrencia de un cuidado ya realizado"
            );
        }

        if (record.status() == CareRecordStatus.COMPLETED && petChanged) {
            throw new NotAllowedException(
                    "No puedes cambiar la mascota de un cuidado ya realizado"
            );
        }

        // Mascota nueva: debe ser activa y del usuario. Misma mascota: se conserva
        // aunque haya sido desactivada después (el cuidado no se borra).
        Pet pet = petChanged
                ? findActiveOwnedPet(petId, user.id())
                : findPetIncludingInactive(record.petId());

        UUID assigneeId = resolveAssigneeId(assigneeEmail);

        CareRecord updated = new CareRecord(
                record.id(),
                user.id(),
                pet.id(),
                care,
                careDate,
                record.createdAt(),
                record.status(),
                assigneeId,
                recurrenceIntervalDays,
                record.originalCareDate(),
                record.seriesId(),
                record.changeReason()
        );

        CareRecord saved = careRecordRepository.save(updated);

        if (dateChanged) {
            careHistoryRepository.save(new CareHistoryEntry(
                    UUID.randomUUID(),
                    user.id(),
                    record.id(),
                    pet.id(),
                    pet.name(),      // snapshot al momento del evento
                    updated.care(),
                    CareHistoryAction.RESCHEDULED,
                    record.careDate(),
                    updated.careDate(),
                    Instant.now(),
                    null
            ));
        }

        return toView(saved, pet);
    }

    @Transactional
    public CareRecordView reschedule(
            String email,
            UUID id,
            LocalDate newCareDate,
            String reason,
            String scope
    ) {
        if (reason == null || reason.isBlank()) {
            throw new NotAllowedException("The rescheduling reason is required");
        }
        validateCareDate(newCareDate);
        if (!"single".equals(scope) && !"future".equals(scope)) {
            throw new NotAllowedException("The rescheduling scope is invalid");
        }

        User user = findUser(email);
        CareRecord record = careRecordRepository.findById(id)
                .filter(careRecord -> careRecord.userId().equals(user.id()))
                .orElseThrow(() -> new NotFoundException("Care record not found"));

        if (record.status() == CareRecordStatus.COMPLETED
                || record.status() == CareRecordStatus.CANCELLED
                || record.careDate().isBefore(LocalDate.now())) {
            throw new NotAllowedException("Past or completed care records cannot be rescheduled");
        }

        Pet pet = findPetIncludingInactive(record.petId());
        long difference = newCareDate.toEpochDay() - record.careDate().toEpochDay();
        CareRecord saved = saveRescheduled(record, newCareDate, reason, user.id(), pet);

        if ("future".equals(scope) && record.recurrenceIntervalDays() != null
                && record.seriesId() != null) {
            for (CareRecord future : careRecordRepository.findPendingBySeriesIdAfter(
                    record.seriesId(), LocalDate.now())) {
                if (future.id().equals(record.id())) {
                    continue;
                }
                saveRescheduled(
                        future,
                        future.careDate().plusDays(difference),
                        reason,
                        user.id(),
                        findPetIncludingInactive(future.petId())
                );
            }
        }

        return toView(saved, pet);
    }

    private CareRecord saveRescheduled(
            CareRecord record,
            LocalDate newCareDate,
            String reason,
            UUID userId,
            Pet pet
    ) {
        CareRecord updated = new CareRecord(
                record.id(),
                record.userId(),
                record.petId(),
                record.care(),
                newCareDate,
                record.createdAt(),
                CareRecordStatus.RESCHEDULED,
                record.assignedToUserId(),
                record.recurrenceIntervalDays(),
                record.originalCareDate(),
                record.seriesId(),
                reason.trim()
        );
        CareRecord saved = careRecordRepository.save(updated);
        careHistoryRepository.save(new CareHistoryEntry(
                UUID.randomUUID(),
                userId,
                record.id(),
                pet.id(),
                pet.name(),
                record.care(),
                CareHistoryAction.RESCHEDULED,
                record.careDate(),
                newCareDate,
                Instant.now(),
                reason.trim()
        ));
        return saved;
    }

    @Transactional
    public void delete(String email, UUID id) {
        User user = findUser(email);

        CareRecord record = careRecordRepository.findById(id)
                .filter(careRecord -> careRecord.userId().equals(user.id()))
                .orElseThrow(() -> new NotFoundException("Cuidado no encontrado"));

        if (record.status() == CareRecordStatus.COMPLETED
                || record.status() == CareRecordStatus.CANCELLED
                || record.careDate().isBefore(LocalDate.now())) {
            throw new NotAllowedException("Past or completed care records cannot be deleted");
        }

        careRecordRepository.deleteById(record.id());
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private Pet findActiveOwnedPet(UUID petId, UUID ownerId) {
        return petRepository.findActiveByIdAndOwnerId(petId, ownerId)
                .orElseThrow(() -> new NotFoundException("Mascota no encontrada"));
    }

    private Pet findPetIncludingInactive(UUID petId) {
        return petRepository.findById(petId)
                .orElseThrow(() -> new NotFoundException("Mascota no encontrada"));
    }

    private CareRecordView toView(CareRecord record, Pet pet) {
        if (pet == null) {
            throw new IllegalStateException("El cuidado " + record.id() + " referencia una mascota inexistente");
        }
        return new CareRecordView(record, pet.name(), pet.species(), pet.isActive());
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

    private void validateCareDate(LocalDate careDate) {
        if (careDate != null && careDate.isBefore(LocalDate.now())) {
            throw new NotAllowedException("La fecha no puede ser anterior a hoy");
        }
    }
}