package com.sessionintelligence.core;

import java.util.List;
import java.util.Objects;

public class SessionIntelligenceEngine {
    private final SessionObservationStore sessionObservationStore;
    private final WindowObservationStore windowObservationStore;
    private final SessionRiskScoreStore riskScoreStore;
    private final FingerprintStrategy fingerprintStrategy;
    private final List<RiskScorer> riskScorers;
    private final List<AnomalyDetector> anomalyDetectors;
    private final List<SessionIntelligenceListener> listeners;

    public SessionIntelligenceEngine(
            SessionObservationStore sessionObservationStore,
            WindowObservationStore windowObservationStore,
            SessionRiskScoreStore riskScoreStore,
            FingerprintStrategy fingerprintStrategy,
            List<RiskScorer> riskScorers,
            List<AnomalyDetector> anomalyDetectors,
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
        this.riskScoreStore = Objects.requireNonNull(riskScoreStore, "riskScoreStore");
        this.fingerprintStrategy = Objects.requireNonNull(
                fingerprintStrategy,
                "fingerprintStrategy"
        );
        this.riskScorers = riskScorers == null ? List.of() : List.copyOf(riskScorers);
        this.anomalyDetectors = anomalyDetectors == null ? List.of() : List.copyOf(anomalyDetectors);
        this.listeners = listeners == null ? List.of() : List.copyOf(listeners);
    }

    public void observe(RequestObservation observation) {
        Objects.requireNonNull(observation, "observation");
        Fingerprint fingerprint = fingerprintStrategy.fingerprint(observation);
        SessionSnapshot sessionSnapshot = sessionObservationStore.record(observation, fingerprint);
        WindowSnapshot windowSnapshot = windowObservationStore.record(observation, fingerprint);
        SessionRiskScore score = buildRiskScore(observation, sessionSnapshot, windowSnapshot);
        if (score != null) {
            riskScoreStore.save(score);
            for (SessionIntelligenceListener listener : listeners) {
                listener.onRiskScoreUpdated(score);
            }
        }
        List<AnomalyEvent> anomalies = detectAnomalies(observation, sessionSnapshot, windowSnapshot);
        for (AnomalyEvent anomaly : anomalies) {
            for (SessionIntelligenceListener listener : listeners) {
                listener.onAnomalyDetected(anomaly);
            }
        }
    }

    private SessionRiskScore buildRiskScore(
            RequestObservation observation,
            SessionSnapshot sessionSnapshot,
            WindowSnapshot windowSnapshot
    ) {
        if (riskScorers.isEmpty()) {
            return null;
        }
        for (RiskScorer scorer : riskScorers) {
            SessionRiskScore scored = scorer.score(observation, sessionSnapshot, windowSnapshot);
            if (scored != null) {
                return scored;
            }
        }
        return new SessionRiskScore(
                observation.sessionKey(),
                0,
                List.of(),
                EvidenceSummary.from(sessionSnapshot, windowSnapshot),
                observation.timestamp()
        );
    }

    private List<AnomalyEvent> detectAnomalies(
            RequestObservation observation,
            SessionSnapshot sessionSnapshot,
            WindowSnapshot windowSnapshot
    ) {
        if (anomalyDetectors.isEmpty()) {
            return List.of();
        }
        List<AnomalyEvent> anomalies = new java.util.ArrayList<>();
        for (AnomalyDetector detector : anomalyDetectors) {
            List<AnomalyEvent> detected = detector.detect(observation, sessionSnapshot, windowSnapshot);
            if (detected != null && !detected.isEmpty()) {
                anomalies.addAll(detected);
            }
        }
        return anomalies;
    }
}
