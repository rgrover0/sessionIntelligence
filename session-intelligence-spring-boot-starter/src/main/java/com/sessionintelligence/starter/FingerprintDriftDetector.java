package com.sessionintelligence.starter;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.sessionintelligence.core.DetectionContext;
import com.sessionintelligence.core.DetectorFinding;
import com.sessionintelligence.core.EvidenceSummary;
import com.sessionintelligence.core.ObservationDetector;
import com.sessionintelligence.core.ReasonCode;
import com.sessionintelligence.core.Severity;
import com.sessionintelligence.core.SessionSnapshot;

public class FingerprintDriftDetector implements ObservationDetector {
    private final SessionIntelligenceProperties properties;

    public FingerprintDriftDetector(SessionIntelligenceProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<DetectorFinding> detect(DetectionContext context) {
        if (context == null || context.observation() == null || context.fingerprint() == null) {
            return List.of();
        }
        SessionSnapshot previous = context.sessionUpdate() != null
                ? context.sessionUpdate().previous()
                : null;
        if (previous == null || previous.lastFingerprint() == null) {
            return List.of();
        }
        String current = context.fingerprint().currentHash();
        if (current == null || current.equals(previous.lastFingerprint())) {
            return List.of();
        }
        if (!withinDriftWindow(previous.lastSeen(), context.observation().timestamp())) {
            return List.of();
        }
        DetectorFinding finding = new DetectorFinding(
                severityFromTolerance(),
                ReasonCode.FINGERPRINT_DRIFT,
                EvidenceSummary.from(
                        context.sessionUpdate() != null ? context.sessionUpdate().current() : null,
                        context.windowUpdate() != null ? context.windowUpdate().current() : null
                ),
                context.observation().timestamp()
        );
        return List.of(finding);
    }

    private boolean withinDriftWindow(Instant lastSeen, Instant now) {
        Duration window = properties.getThresholds().getFingerprintDriftWindow();
        if (window == null || window.isZero() || window.isNegative() || lastSeen == null || now == null) {
            return true;
        }
        Duration delta = Duration.between(lastSeen, now).abs();
        return delta.compareTo(window) <= 0;
    }

    private Severity severityFromTolerance() {
        double tolerance = properties.getThresholds().getFingerprintDriftTolerance();
        if (tolerance <= 0.25d) {
            return Severity.HIGH;
        }
        if (tolerance <= 0.5d) {
            return Severity.MEDIUM;
        }
        return Severity.LOW;
    }
}
