package com.ihc.mascotas.backend.care.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ihc.mascotas.backend.care.domain.CareRecordStatus;

public interface SpringDataCareRecordRepository extends JpaRepository<CareRecordJpaEntity, UUID> {
    List<CareRecordJpaEntity> findByUserId(UUID userId);

    @Query("""
            SELECT record
            FROM CareRecordJpaEntity record
            WHERE record.userId = :userId
            AND (:petName = '' OR LOWER(record.petName) LIKE LOWER(CONCAT('%', :petName, '%')))              AND (:status IS NULL OR record.status = :status)
              AND (
                    :careType = ''
                    OR (:careType = 'VACCINE' AND LOWER(record.care) LIKE '%vacuna%')
                    OR (:careType = 'MEDICATION' AND LOWER(record.care) LIKE '%medicamento%')
                    OR (:careType = 'CONTROL' AND LOWER(record.care) LIKE '%control%')
                    OR (:careType = 'OTHER'
                        AND LOWER(record.care) NOT LIKE '%vacuna%'
                        AND LOWER(record.care) NOT LIKE '%medicamento%'
                        AND LOWER(record.care) NOT LIKE '%control%')
                  )
              AND (:fromDate IS NULL OR record.careDate >= :fromDate)
              AND (:toDate IS NULL OR record.careDate <= :toDate)
            ORDER BY record.careDate ASC
            """)
    List<CareRecordJpaEntity> findByFilters(
            @Param("userId") UUID userId,
            @Param("petName") String petName,
            @Param("status") CareRecordStatus status,
            @Param("careType") String careType,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}
