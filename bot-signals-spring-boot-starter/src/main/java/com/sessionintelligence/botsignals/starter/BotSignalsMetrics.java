package com.sessionintelligence.botsignals.starter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;

import com.sessionintelligence.botsignals.AnomalyEvent;
import com.sessionintelligence.botsignals.EvidenceSummary;
import com.sessionintelligence.botsignals.RiskBand;
import com.sessionintelligence.botsignals.SessionRiskScore;

public class BotSignalsMetrics {
    private final MeterRegistry registry;

    public BotSignalsMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordRiskScore(SessionRiskScore score) {
        if (score == null) {
            return;
        }
        RiskBand band = RiskBand.fromScore(score.score());
        DistributionSummary.builder("bot_signals_risk_score")
                .tag("band", band.name().toLowerCase())
                .register(registry)
                .record(score.score());
    }

    public void recordAnomaly(AnomalyEvent event) {
        if (event == null || event.reasonCode() == null) {
            return;
        }
        Counter.builder("bot_signals_anomalies")
                .tag("reason", event.reasonCode().name().toLowerCase())
                .register(registry)
                .increment();
    }

    public void recordRate(String scope, double rate) {
        if (scope == null || scope.isBlank() || rate < 0) {
            return;
        }
        DistributionSummary.builder("bot_signals_rate_endpoint_rate")
                .tag("scope", scope)
                .register(registry)
                .record(rate);
    }

    public void recordWindowCount(EvidenceSummary summary) {
        if (summary == null || summary.sessionWindowCount() < 0) {
            return;
        }
        DistributionSummary.builder("bot_signals_windows_per_session")
                .register(registry)
                .record(summary.sessionWindowCount());
    }
}
