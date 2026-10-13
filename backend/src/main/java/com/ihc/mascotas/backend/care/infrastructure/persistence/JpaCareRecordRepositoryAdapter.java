package com.ihc.mascotas.backend.care.infrastructure.persistence;

import com.ihc.mascotas.backend.care.domain.CareRecord;
import com.ihc.mascotas.backend.care.domain.CareRecordRepository;
import com.ihc.mascotas.backend.care.domain.CareRecordStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaCareRecordRepositoryAdapter implements CareRecordRepository {

    private final SpringDataCareRecordRepository repository;

    public JpaCareRecordRepositoryAdapter(SpringDataCareRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    public CareRecord save(CareRecord careRecord) {
        CareRecordJpaEntity entity = repository.save(toEntity(careRecord));

        return toDomain(entity);
    }

    @Override
    public List<CareRecord> findByUserId(UUID userId) {
        return repository.findByUserId(userId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<CareRecord> findByFilters(
            UUID userId,
            String petName,
            CareRecordStatus status,
            String careType,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return repository.findByFilters(userId, petName, status, careType, fromDate, toDate)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<CareRecord> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    private CareRecordJpaEntity toEntity(CareRecord careRecord) {
        return new CareRecordJpaEntity(
                careRecord.id(),
                careRecord.userId(),
                careRecord.petName(),
                careRecord.care(),
                careRecord.animalType(),
                careRecord.careDate(),
                careRecord.createdAt(),
                careRecord.status()
        );
    }

    private CareRecord toDomain(CareRecordJpaEntity careRecord) {
        return new CareRecord(
                careRecord.getId(),
                careRecord.getUserId(),
                careRecord.getPetName(),
                careRecord.getCare(),
                careRecord.getAnimalType(),
                careRecord.getCareDate(),
                careRecord.getCreatedAt(),
                careRecord.getStatus()
        );
    }
}
