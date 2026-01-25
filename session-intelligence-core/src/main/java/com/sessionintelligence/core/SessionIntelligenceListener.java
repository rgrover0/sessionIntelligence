package com.sessionintelligence.core;

public interface SessionIntelligenceListener {
    default void onRiskScoreUpdated(SessionRiskScore score) {
        // no-op by default
    }

    default void onAnomalyDetected(AnomalyEvent event) {
        // no-op by default
    }
}
