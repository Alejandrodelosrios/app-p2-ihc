package com.ihc.mascotas.backend.analytics.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataAnalyticsEventRepository extends JpaRepository<AnalyticsEventJpaEntity, UUID> {
    long countByName(String name);

    List<AnalyticsEventJpaEntity> findTop20ByOrderByOccurredAtDesc();
}
