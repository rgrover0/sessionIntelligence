package com.sessionintelligence.starter;

import java.time.Duration;
import java.util.List;

import com.sessionintelligence.core.DetectionContext;
import com.sessionintelligence.core.DetectorFinding;
import com.sessionintelligence.core.EvidenceSummary;
import com.sessionintelligence.core.ObservationDetector;
import com.sessionintelligence.core.ReasonCode;
import com.sessionintelligence.core.Severity;
import com.sessionintelligence.core.SessionSnapshot;

public class WindowExplosionDetector implements ObservationDetector {
    private final SessionIntelligenceProperties properties;

    public WindowExplosionDetector(SessionIntelligenceProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<DetectorFinding> detect(DetectionContext context) {
        if (context == null || context.observation() == null) {
            return List.of();
        }
        SessionSnapshot snapshot = context.sessionUpdate() != null
                ? context.sessionUpdate().current()
                : null;
        if (snapshot == null || snapshot.firstSeen() == null) {
            return List.of();
        }
        int maxWindows = properties.getThresholds().getMaxParallelWindows();
        if (snapshot.windowCount() <= maxWindows) {
            return List.of();
        }
        Duration window = properties.getThresholds().getWindowExplosionWindow();
        Duration elapsed = Duration.between(snapshot.firstSeen(), context.observation().timestamp()).abs();
        if (window != null && !window.isZero() && !window.isNegative() && elapsed.compareTo(window) > 0) {
            return List.of();
        }
        DetectorFinding finding = new DetectorFinding(
                Severity.HIGH,
                ReasonCode.WINDOW_EXPLOSION,
                EvidenceSummary.from(
                        context.sessionUpdate() != null ? context.sessionUpdate().current() : null,
                        context.windowUpdate() != null ? context.windowUpdate().current() : null
                ),
                context.observation().timestamp()
        );
        return List.of(finding);
    }
}
