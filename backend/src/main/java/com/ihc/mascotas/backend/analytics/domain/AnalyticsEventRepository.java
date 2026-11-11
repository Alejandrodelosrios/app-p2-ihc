package com.ihc.mascotas.backend.analytics.domain;

import java.util.List;

public interface AnalyticsEventRepository {
    AnalyticsEvent save(AnalyticsEvent event);

    long countByName(String name);

    List<AnalyticsEvent> findRecent();
}
