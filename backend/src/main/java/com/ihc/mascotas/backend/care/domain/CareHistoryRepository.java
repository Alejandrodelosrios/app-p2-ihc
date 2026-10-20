package com.ihc.mascotas.backend.care.domain;

public interface CareHistoryRepository {
    CareHistoryEntry save(CareHistoryEntry entry);
}
