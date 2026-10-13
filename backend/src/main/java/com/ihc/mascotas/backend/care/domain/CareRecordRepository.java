package com.ihc.mascotas.backend.care.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CareRecordRepository {

    CareRecord save(CareRecord careRecord);

    List<CareRecord> findByUserId(UUID userId);

    List<CareRecord> findByFilters(
            UUID userId,
            String petName,
            CareRecordStatus status,
            String careType,
            LocalDate fromDate,
            LocalDate toDate
    );

    Optional<CareRecord> findById(UUID id);

    void deleteById(UUID id);
}
