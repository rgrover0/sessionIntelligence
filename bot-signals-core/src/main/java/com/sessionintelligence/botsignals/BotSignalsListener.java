package com.sessionintelligence.botsignals;

public interface BotSignalsListener {
    default void onRiskScoreUpdated(SessionRiskScore score) {
        // no-op by default
    }

    default void onAnomalyDetected(AnomalyEvent event) {
        // no-op by default
    }
}
