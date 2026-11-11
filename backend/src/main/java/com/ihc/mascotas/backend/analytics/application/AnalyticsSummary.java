package com.ihc.mascotas.backend.analytics.application;

import com.ihc.mascotas.backend.analytics.domain.AnalyticsEvent;

import java.util.List;

public record AnalyticsSummary(
        long started,
        long completed,
        long conversionPercent,
        List<AnalyticsEvent> recent
) {
}
