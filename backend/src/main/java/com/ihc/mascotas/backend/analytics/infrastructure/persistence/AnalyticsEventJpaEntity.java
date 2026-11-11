package com.ihc.mascotas.backend.analytics.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "eventos")
public class AnalyticsEventJpaEntity {
    @Id
    private UUID id;

    @Column(name = "evento", nullable = false)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "props", nullable = false)
    private Map<String, String> props;

    @Column(name = "sesion")
    private String sessionId;

    @Column(name = "fecha", nullable = false)
    private Instant occurredAt;

    protected AnalyticsEventJpaEntity() {
    }

    public AnalyticsEventJpaEntity(UUID id, String name, Map<String, String> props, String sessionId, Instant occurredAt) {
        this.id = id;
        this.name = name;
        this.props = props;
        this.sessionId = sessionId;
        this.occurredAt = occurredAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public Map<String, String> getProps() { return props; }
    public String getSessionId() { return sessionId; }
    public Instant getOccurredAt() { return occurredAt; }
}
