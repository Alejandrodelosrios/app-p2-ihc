package com.ihc.mascotas.backend.care.infrastructure.persistence;

import com.ihc.mascotas.backend.care.domain.CareRecordStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "care_record")
public class CareRecordJpaEntity {
    @Id private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "pet_id", nullable = false)
    private UUID petId;

    @Column(name = "care")
    private String care;

    @Column(name = "care_date")
    private LocalDate careDate;

    @Column(name = "created_at")
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CareRecordStatus status;

    @Column(name = "assigned_to_user_id")
    private UUID assignedToUserId;

    @Column(name = "recurrence_interval_days")
    private Integer recurrenceIntervalDays;

    @Column(name = "original_care_date", nullable = false)
    private LocalDate originalCareDate;

    @Column(name = "series_id")
    private UUID seriesId;

    @Column(name = "change_reason")
    private String changeReason;

    protected CareRecordJpaEntity() {}

    public CareRecordJpaEntity(UUID id, UUID userId, UUID petId, String care, LocalDate careDate,
                               Instant createdAt, CareRecordStatus status, UUID assignedToUserId,
                               Integer recurrenceIntervalDays, LocalDate originalCareDate, UUID seriesId,
                               String changeReason) {
        this.id = id;
        this.userId = userId;
        this.petId = petId;
        this.care = care;
        this.careDate = careDate;
        this.createdAt = createdAt;
        this.status = status;
        this.assignedToUserId = assignedToUserId;
        this.recurrenceIntervalDays = recurrenceIntervalDays;
        this.originalCareDate = originalCareDate;
        this.seriesId = seriesId;
        this.changeReason = changeReason;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getPetId() { return petId; }
    public String getCare() { return care; }
    public LocalDate getCareDate() { return careDate; }
    public Instant getCreatedAt() { return createdAt; }
    public CareRecordStatus getStatus() { return status; }
    public UUID getAssignedToUserId() { return assignedToUserId; }
    public Integer getRecurrenceIntervalDays() { return recurrenceIntervalDays; }
    public LocalDate getOriginalCareDate() { return originalCareDate; }
    public UUID getSeriesId() { return seriesId; }
    public String getChangeReason() { return changeReason; }
}