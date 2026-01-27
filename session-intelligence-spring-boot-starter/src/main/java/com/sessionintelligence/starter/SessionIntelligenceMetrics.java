package com.sessionintelligence.starter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;

import com.sessionintelligence.core.AnomalyEvent;
import com.sessionintelligence.core.EvidenceSummary;
import com.sessionintelligence.core.RiskBand;
import com.sessionintelligence.core.SessionRiskScore;

public class SessionIntelligenceMetrics {
    private final MeterRegistry registry;

    public SessionIntelligenceMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordRiskScore(SessionRiskScore score) {
        if (score == null) {
            return;
        }
        RiskBand band = RiskBand.fromScore(score.score());
        DistributionSummary.builder("session_intelligence_risk_score")
                .tag("band", band.name().toLowerCase())
                .register(registry)
                .record(score.score());
    }

    public void recordAnomaly(AnomalyEvent event) {
        if (event == null || event.reasonCode() == null) {
            return;
        }
        Counter.builder("session_intelligence_anomalies")
                .tag("reason", event.reasonCode().name().toLowerCase())
                .register(registry)
                .increment();
    }

    public void recordRate(String scope, double rate) {
        if (scope == null || scope.isBlank() || rate < 0) {
            return;
        }
        DistributionSummary.builder("session_intelligence_rate_endpoint_rate")
                .tag("scope", scope)
                .register(registry)
                .record(rate);
    }

    public void recordWindowCount(EvidenceSummary summary) {
        if (summary == null || summary.sessionWindowCount() < 0) {
            return;
        }
        DistributionSummary.builder("session_intelligence_windows_per_session")
                .register(registry)
                .record(summary.sessionWindowCount());
    }
}
