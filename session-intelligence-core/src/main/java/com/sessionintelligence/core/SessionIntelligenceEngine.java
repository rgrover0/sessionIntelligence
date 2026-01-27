package com.sessionintelligence.core;

import java.util.List;
import java.util.Objects;

public class SessionIntelligenceEngine {
    private final ObservationStore observationStore;
    private final FingerprintStrategy fingerprintStrategy;
    private final List<RiskScorer> riskScorers;
    private final List<ObservationDetector> detectors;
    private final List<SessionIntelligenceListener> listeners;

    public SessionIntelligenceEngine(
            ObservationStore observationStore,
            FingerprintStrategy fingerprintStrategy,
            List<RiskScorer> riskScorers,
            List<ObservationDetector> detectors,
            List<SessionIntelligenceListener> listeners
    ) {
        this.observationStore = Objects.requireNonNull(observationStore, "observationStore");
        this.fingerprintStrategy = Objects.requireNonNull(
                fingerprintStrategy,
                "fingerprintStrategy"
        );
        this.riskScorers = riskScorers == null ? List.of() : List.copyOf(riskScorers);
        this.detectors = detectors == null ? List.of() : List.copyOf(detectors);
        this.listeners = listeners == null ? List.of() : List.copyOf(listeners);
    }

    public void observe(RequestObservation observation) {
        Objects.requireNonNull(observation, "observation");
        Fingerprint fingerprint = fingerprintStrategy.fingerprint(observation);
        SnapshotUpdate<SessionSnapshot> sessionUpdate =
                observationStore.recordSession(observation, fingerprint);
        SnapshotUpdate<WindowSnapshot> windowUpdate =
                observationStore.recordWindow(observation, fingerprint);
        DetectionContext context = new DetectionContext(
                observation,
                sessionUpdate,
                windowUpdate,
                fingerprint
        );
        List<DetectorFinding> findings = runDetectors(context);
        emitAnomalies(findings, observation.sessionKey());
        SessionRiskScore score = buildRiskScore(context, findings);
        if (score != null) {
            observationStore.save(score);
            for (SessionIntelligenceListener listener : listeners) {
                listener.onRiskScoreUpdated(score);
            }
        }
    }

    private SessionRiskScore buildRiskScore(
            DetectionContext context,
            List<DetectorFinding> findings
    ) {
        if (riskScorers.isEmpty()) {
            return null;
        }
        for (RiskScorer scorer : riskScorers) {
            SessionRiskScore scored = scorer.score(context, findings);
            if (scored != null) {
                return scored;
            }
        }
        return null;
    }

    private List<DetectorFinding> runDetectors(DetectionContext context) {
        if (detectors.isEmpty()) {
            return List.of();
        }
        List<DetectorFinding> findings = new java.util.ArrayList<>();
        for (ObservationDetector detector : detectors) {
            List<DetectorFinding> detected = detector.detect(context);
            if (detected != null && !detected.isEmpty()) {
                findings.addAll(detected);
            }
        }
        return findings;
    }

    private void emitAnomalies(List<DetectorFinding> findings, SessionKey sessionKey) {
        if (findings == null || findings.isEmpty()) {
            return;
        }
        for (DetectorFinding finding : findings) {
            AnomalyEvent event = new AnomalyEvent(
                    finding.detectedAt(),
                    sessionKey,
                    finding.severity(),
                    finding.reasonCode(),
                    finding.evidenceSummary(),
                    null
            );
            for (SessionIntelligenceListener listener : listeners) {
                listener.onAnomalyDetected(event);
            }
        }
    }
}
