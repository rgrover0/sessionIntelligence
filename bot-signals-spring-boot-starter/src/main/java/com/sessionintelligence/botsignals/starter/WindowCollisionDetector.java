package com.sessionintelligence.botsignals.starter;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.sessionintelligence.botsignals.DetectionContext;
import com.sessionintelligence.botsignals.DetectorFinding;
import com.sessionintelligence.botsignals.EvidenceSummary;
import com.sessionintelligence.botsignals.ObservationDetector;
import com.sessionintelligence.botsignals.ReasonCode;
import com.sessionintelligence.botsignals.Severity;
import com.sessionintelligence.botsignals.WindowSnapshot;

public class WindowCollisionDetector implements ObservationDetector {
    private final BotSignalsProperties properties;

    public WindowCollisionDetector(BotSignalsProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<DetectorFinding> detect(DetectionContext context) {
        if (context == null || context.observation() == null || context.fingerprint() == null) {
            return List.of();
        }
        WindowSnapshot previous = context.windowUpdate() != null
                ? context.windowUpdate().previous()
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
                Severity.HIGH,
                ReasonCode.WINDOW_COLLISION,
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
}
