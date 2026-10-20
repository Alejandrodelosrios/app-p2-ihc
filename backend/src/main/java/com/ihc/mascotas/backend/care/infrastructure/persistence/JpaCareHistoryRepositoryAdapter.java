package com.ihc.mascotas.backend.care.infrastructure.persistence;

import com.ihc.mascotas.backend.care.domain.CareHistoryEntry;
import com.ihc.mascotas.backend.care.domain.CareHistoryRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaCareHistoryRepositoryAdapter implements CareHistoryRepository {
    private final SpringDataCareHistoryRepository repository;

    public JpaCareHistoryRepositoryAdapter(SpringDataCareHistoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public CareHistoryEntry save(CareHistoryEntry entry) {
        return toDomain(repository.save(toEntity(entry)));
    }

    private CareHistoryJpaEntity toEntity(CareHistoryEntry entry) {
        return new CareHistoryJpaEntity(
                entry.id(),
                entry.userId(),
                entry.careRecordId(),
                entry.petName(),
                entry.care(),
                entry.action(),
                entry.previousCareDate(),
                entry.currentCareDate(),
                entry.occurredAt()
        );
    }

    private CareHistoryEntry toDomain(CareHistoryJpaEntity entry) {
        return new CareHistoryEntry(
                entry.getId(),
                entry.getUserId(),
                entry.getCareRecordId(),
                entry.getPetName(),
                entry.getCare(),
                entry.getAction(),
                entry.getPreviousCareDate(),
                entry.getCurrentCareDate(),
                entry.getOccurredAt()
        );
    }
}
