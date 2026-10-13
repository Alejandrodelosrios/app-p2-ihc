package com.ihc.mascotas.backend.care.application;

import com.ihc.mascotas.backend.auth.domain.User;
import com.ihc.mascotas.backend.auth.domain.UserRepository;
import com.ihc.mascotas.backend.care.domain.CareRecord;
import com.ihc.mascotas.backend.care.domain.CareRecordRepository;
import com.ihc.mascotas.backend.care.domain.CareRecordStatus;
import com.ihc.mascotas.backend.shared.exception.ConflictException;
import com.ihc.mascotas.backend.shared.exception.ForbiddenException;
import com.ihc.mascotas.backend.shared.exception.NotAllowedException;
import com.ihc.mascotas.backend.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class CareRecordService {
    private final CareRecordRepository careRecordRepository;
    private final UserRepository userRepository;

    public CareRecordService(CareRecordRepository careRecordRepository, UserRepository userRepository) {
        this.careRecordRepository = careRecordRepository;
        this.userRepository = userRepository;
    }

    public CareRecord create(
            String email,
            String petName,
            String care,
            String animalType,
            LocalDate careDate
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        CareRecord record = new CareRecord(
                UUID.randomUUID(),
                user.id(),
                petName,
                care,
                animalType,
                careDate,
                Instant.now(),
                CareRecordStatus.PENDING
        );

        return careRecordRepository.save(record);
    }

    public List<CareRecord> findByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return careRecordRepository.findByUserId(user.id());
    }

    public CareRecord complete(String email, UUID id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        CareRecord record = careRecordRepository.findById(id)
                .filter(careRecord -> careRecord.userId().equals(user.id()))
                .orElseThrow(() -> new ForbiddenException("Cuidado no encontrado"));

        if (record.status() == CareRecordStatus.COMPLETED) {
            throw new ConflictException("El cuidado ya fue realizado");
        }

        CareRecord completed = new CareRecord(
                record.id(),
                record.userId(),
                record.petName(),
                record.care(),
                record.animalType(),
                record.careDate(),
                record.createdAt(),
                CareRecordStatus.COMPLETED
        );

        return careRecordRepository.save(completed);
    }

    public CareRecord update(
            String email,
            UUID id,
            String petName,
            String care,
            String animalType,
            LocalDate careDate
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        CareRecord record = careRecordRepository.findById(id)
                .filter(careRecord -> careRecord.userId().equals(user.id()))
                .orElseThrow(() -> new NotFoundException("Cuidado no encontrado"));

        boolean dateChanged = !record.careDate().equals(careDate);

        if (record.status() == CareRecordStatus.COMPLETED && dateChanged) {
            throw new NotAllowedException("No puedes reprogramar un cuidado ya hecho");
        }

        if (dateChanged && careDate.isBefore(LocalDate.now())) {
            throw new NotAllowedException("La nueva fecha no puede ser anterior a hoy");
        }

        CareRecord updated = new CareRecord(
                record.id(),
                record.userId(),
                petName,
                care,
                animalType,
                careDate,
                record.createdAt(),
                record.status()
        );

        return careRecordRepository.save(updated);
    }

    public void delete(String email, UUID id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        CareRecord record = careRecordRepository.findById(id)
                .filter(careRecord -> careRecord.userId().equals(user.id()))
                .orElseThrow(() -> new NotFoundException("Cuidado no encontrado"));

        careRecordRepository.deleteById(record.id());
    }
}
