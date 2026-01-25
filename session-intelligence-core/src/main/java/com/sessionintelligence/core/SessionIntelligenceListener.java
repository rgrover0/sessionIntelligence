package com.sessionintelligence.core;

public interface SessionIntelligenceListener {
    default void onObservation(RequestObservation observation) {
        // no-op by default
    }

    default void onAnomaly(AnomalyEvent event) {
        // no-op by default
    }
}
