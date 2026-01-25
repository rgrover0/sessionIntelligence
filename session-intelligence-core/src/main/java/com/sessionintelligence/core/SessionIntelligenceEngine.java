package com.sessionintelligence.core;

import java.util.List;
import java.util.Objects;

public class SessionIntelligenceEngine {
    private final ObservationStore observationStore;
    private final List<SessionIntelligenceListener> listeners;

    public SessionIntelligenceEngine(
            ObservationStore observationStore,
            List<SessionIntelligenceListener> listeners
    ) {
        this.observationStore = Objects.requireNonNull(observationStore, "observationStore");
        this.listeners = listeners == null ? List.of() : List.copyOf(listeners);
    }

    public void observe(RequestObservation observation) {
        Objects.requireNonNull(observation, "observation");
        observationStore.save(observation);
        for (SessionIntelligenceListener listener : listeners) {
            listener.onObservation(observation);
        }
    }
}
