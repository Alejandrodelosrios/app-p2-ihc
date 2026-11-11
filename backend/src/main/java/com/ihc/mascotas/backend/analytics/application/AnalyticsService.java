package com.ihc.mascotas.backend.analytics.application;

import com.ihc.mascotas.backend.analytics.domain.AnalyticsEvent;
import com.ihc.mascotas.backend.analytics.domain.AnalyticsEventRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class AnalyticsService {
    private final AnalyticsEventRepository repository;

    public AnalyticsService(AnalyticsEventRepository repository) {
        this.repository = repository;
    }

    /** Registra un evento. No recibe ni guarda el usuario: se registra la acción, no a la persona. */
    public void track(String name, Map<String, String> props, String sessionId) {
        repository.save(new AnalyticsEvent(
                UUID.randomUUID(),
                name,
                props,
                sessionId,
                Instant.now()
        ));
    }

    public AnalyticsSummary summary() {
        long started = repository.countByName(AnalyticsEvent.CARE_OPENED);
        long completed = repository.countByName(AnalyticsEvent.CARE_COMPLETED);
        long conversion = started == 0 ? 0 : Math.round(completed * 100.0 / started);
        return new AnalyticsSummary(started, completed, conversion, repository.findRecent());
    }
}
