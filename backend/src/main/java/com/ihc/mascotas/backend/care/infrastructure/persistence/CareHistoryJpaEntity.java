package com.ihc.mascotas.backend.care.infrastructure.persistence;

import com.ihc.mascotas.backend.care.domain.CareHistoryAction;
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
@Table(name = "care_history_entry")
public class CareHistoryJpaEntity {
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "care_record_id", nullable = false)
    private UUID careRecordId;

    @Column(name = "pet_name", nullable = false)
    private String petName;

    @Column(name = "care")
    private String care;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private CareHistoryAction action;

    @Column(name = "previous_care_date")
    private LocalDate previousCareDate;

    @Column(name = "current_care_date")
    private LocalDate currentCareDate;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected CareHistoryJpaEntity() {
    }

    public CareHistoryJpaEntity(
            UUID id,
            UUID userId,
            UUID careRecordId,
            String petName,
            String care,
            CareHistoryAction action,
            LocalDate previousCareDate,
            LocalDate currentCareDate,
            Instant occurredAt
    ) {
        this.id = id;
        this.userId = userId;
        this.careRecordId = careRecordId;
        this.petName = petName;
        this.care = care;
        this.action = action;
        this.previousCareDate = previousCareDate;
        this.currentCareDate = currentCareDate;
        this.occurredAt = occurredAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getCareRecordId() { return careRecordId; }
    public String getPetName() { return petName; }
    public String getCare() { return care; }
    public CareHistoryAction getAction() { return action; }
    public LocalDate getPreviousCareDate() { return previousCareDate; }
    public LocalDate getCurrentCareDate() { return currentCareDate; }
    public Instant getOccurredAt() { return occurredAt; }
}
