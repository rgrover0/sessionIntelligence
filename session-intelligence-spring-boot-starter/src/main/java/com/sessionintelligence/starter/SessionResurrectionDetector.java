package com.sessionintelligence.starter;

import java.util.List;

import com.sessionintelligence.core.DetectionContext;
import com.sessionintelligence.core.DetectorFinding;
import com.sessionintelligence.core.EvidenceSummary;
import com.sessionintelligence.core.ObservationDetector;
import com.sessionintelligence.core.ReasonCode;
import com.sessionintelligence.core.Severity;
import com.sessionintelligence.core.SessionSnapshot;

public class SessionResurrectionDetector implements ObservationDetector {
    @Override
    public List<DetectorFinding> detect(DetectionContext context) {
        if (context == null || context.observation() == null || context.sessionUpdate() == null) {
            return List.of();
        }
        SessionSnapshot previous = context.sessionUpdate().previous();
        SessionSnapshot current = context.sessionUpdate().current();
        if (previous == null || current == null) {
            return List.of();
        }
        if (current.requestCount() < previous.requestCount()) {
            DetectorFinding finding = new DetectorFinding(
                    Severity.HIGH,
                    ReasonCode.SESSION_RESURRECTION,
                    EvidenceSummary.from(
                            current,
                            context.windowUpdate() != null ? context.windowUpdate().current() : null
                    ),
                    context.observation().timestamp()
            );
            return List.of(finding);
        }
        if (current.firstSeen() != null
                && current.lastSeen() != null
                && current.lastSeen().isBefore(current.firstSeen())) {
            DetectorFinding finding = new DetectorFinding(
                    Severity.MEDIUM,
                    ReasonCode.SESSION_RESURRECTION,
                    EvidenceSummary.from(
                            current,
                            context.windowUpdate() != null ? context.windowUpdate().current() : null
                    ),
                    context.observation().timestamp()
            );
            return List.of(finding);
        }
        return List.of();
    }
}
