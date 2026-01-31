package com.sessionintelligence.botsignals;

import java.time.Instant;

public record EvidenceSummary(
        long sessionRequestCount,
        long sessionWindowCount,
        long windowRequestCount,
        Instant sessionFirstSeen,
        Instant sessionLastSeen,
        Instant windowFirstSeen,
        Instant windowLastSeen
) {
    public static EvidenceSummary from(SessionSnapshot session, WindowSnapshot window) {
        long sessionCount = session != null ? session.requestCount() : 0L;
        long sessionWindowCount = session != null ? session.windowCount() : 0L;
        long windowRequestCount = window != null ? window.requestCount() : 0L;
        Instant sessionFirst = session != null ? session.firstSeen() : null;
        Instant sessionLast = session != null ? session.lastSeen() : null;
        Instant windowFirst = window != null ? window.firstSeen() : null;
        Instant windowLast = window != null ? window.lastSeen() : null;
        return new EvidenceSummary(
                sessionCount,
                sessionWindowCount,
                windowRequestCount,
                sessionFirst,
                sessionLast,
                windowFirst,
                windowLast
        );
    }
}
