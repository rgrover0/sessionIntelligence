package com.sessionintelligence.core;

import java.time.Instant;

public record SessionAggregate(
        String sessionId,
        long requestCount,
        Instant lastSeen,
        String lastFingerprint
) {
}
