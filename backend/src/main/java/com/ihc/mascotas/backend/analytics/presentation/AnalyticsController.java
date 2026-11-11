package com.ihc.mascotas.backend.analytics.presentation;

import com.ihc.mascotas.backend.analytics.application.AnalyticsService;
import com.ihc.mascotas.backend.analytics.application.AnalyticsSummary;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void track(@Valid @RequestBody TrackEventRequest request) {
        analyticsService.track(request.event(), request.props(), request.sessionId());
    }

    @GetMapping("/analytics")
    public AnalyticsSummary summary() {
        return analyticsService.summary();
    }
}
