package com.sessionintelligence.core;

import java.util.List;
import java.util.Objects;

public class SessionIntelligenceEngine {
    private final SessionObservationStore sessionObservationStore;
    private final WindowObservationStore windowObservationStore;
    private final RiskSnapshotStore riskSnapshotStore;
    private final FingerprintStrategy fingerprintStrategy;
    private final List<SessionIntelligenceListener> listeners;

    public SessionIntelligenceEngine(
            SessionObservationStore sessionObservationStore,
            WindowObservationStore windowObservationStore,
            RiskSnapshotStore riskSnapshotStore,
            FingerprintStrategy fingerprintStrategy,
            List<SessionIntelligenceListener> listeners
    ) {
        this.sessionObservationStore = Objects.requireNonNull(
                sessionObservationStore,
                "sessionObservationStore"
        );
        this.windowObservationStore = Objects.requireNonNull(
                windowObservationStore,
                "windowObservationStore"
        );
        this.riskSnapshotStore = Objects.requireNonNull(riskSnapshotStore, "riskSnapshotStore");
        this.fingerprintStrategy = Objects.requireNonNull(
                fingerprintStrategy,
                "fingerprintStrategy"
        );
        this.listeners = listeners == null ? List.of() : List.copyOf(listeners);
    }

    public void observe(RequestObservation observation) {
        Objects.requireNonNull(observation, "observation");
        Fingerprint fingerprint = fingerprintStrategy.fingerprint(observation);
        sessionObservationStore.record(observation, fingerprint);
        windowObservationStore.record(observation, fingerprint);
        RiskSnapshot snapshot = new RiskSnapshot(
                observation.timestamp(),
                observation.sessionKey(),
                0.0d,
                "UNSET"
        );
        riskSnapshotStore.save(snapshot);
        for (SessionIntelligenceListener listener : listeners) {
            listener.onObservation(observation);
        }
    }
}
