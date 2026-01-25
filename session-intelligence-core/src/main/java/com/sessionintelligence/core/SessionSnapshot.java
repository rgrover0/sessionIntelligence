package com.sessionintelligence.core;

import java.time.Instant;

public record SessionSnapshot(
        String sessionId,
        long requestCount,
        Instant firstSeen,
        Instant lastSeen,
        String lastFingerprint
) {
}
