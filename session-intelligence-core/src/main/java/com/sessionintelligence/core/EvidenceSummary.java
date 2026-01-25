package com.sessionintelligence.core;

import java.time.Instant;

public record EvidenceSummary(
        long sessionRequestCount,
        long windowRequestCount,
        Instant sessionFirstSeen,
        Instant sessionLastSeen,
        Instant windowFirstSeen,
        Instant windowLastSeen
) {
    public static EvidenceSummary from(SessionSnapshot session, WindowSnapshot window) {
        long sessionCount = session != null ? session.requestCount() : 0L;
        long windowCount = window != null ? window.requestCount() : 0L;
        Instant sessionFirst = session != null ? session.firstSeen() : null;
        Instant sessionLast = session != null ? session.lastSeen() : null;
        Instant windowFirst = window != null ? window.firstSeen() : null;
        Instant windowLast = window != null ? window.lastSeen() : null;
        return new EvidenceSummary(
                sessionCount,
                windowCount,
                sessionFirst,
                sessionLast,
                windowFirst,
                windowLast
        );
    }
}
