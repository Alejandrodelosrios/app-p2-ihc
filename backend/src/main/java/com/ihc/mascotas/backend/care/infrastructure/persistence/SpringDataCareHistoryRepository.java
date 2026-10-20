package com.ihc.mascotas.backend.care.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataCareHistoryRepository extends JpaRepository<CareHistoryJpaEntity, UUID> {
    List<CareHistoryJpaEntity> findByUserIdOrderByOccurredAtDesc(UUID userId);
}
