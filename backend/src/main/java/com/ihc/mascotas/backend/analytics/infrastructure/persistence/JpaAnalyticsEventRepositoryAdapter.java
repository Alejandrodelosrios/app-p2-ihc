package com.ihc.mascotas.backend.analytics.infrastructure.persistence;

import com.ihc.mascotas.backend.analytics.domain.AnalyticsEvent;
import com.ihc.mascotas.backend.analytics.domain.AnalyticsEventRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JpaAnalyticsEventRepositoryAdapter implements AnalyticsEventRepository {
    private final SpringDataAnalyticsEventRepository repository;

    public JpaAnalyticsEventRepositoryAdapter(SpringDataAnalyticsEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public AnalyticsEvent save(AnalyticsEvent event) {
        return toDomain(repository.save(toEntity(event)));
    }

    @Override
    public long countByName(String name) {
        return repository.countByName(name);
    }

    @Override
    public List<AnalyticsEvent> findRecent() {
        return repository.findTop20ByOrderByOccurredAtDesc()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private AnalyticsEventJpaEntity toEntity(AnalyticsEvent event) {
        return new AnalyticsEventJpaEntity(
                event.id(),
                event.name(),
                event.props(),
                event.sessionId(),
                event.occurredAt()
        );
    }

    private AnalyticsEvent toDomain(AnalyticsEventJpaEntity entity) {
        return new AnalyticsEvent(
                entity.getId(),
                entity.getName(),
                entity.getProps(),
                entity.getSessionId(),
                entity.getOccurredAt()
        );
    }
}
