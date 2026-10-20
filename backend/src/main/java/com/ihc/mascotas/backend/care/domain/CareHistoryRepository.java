package com.ihc.mascotas.backend.care.domain;

import java.util.List;
import java.util.UUID;

public interface CareHistoryRepository {
    CareHistoryEntry save(CareHistoryEntry entry);

    List<CareHistoryEntry> findByUserIdOrderByOccurredAtDesc(UUID userId);
}
