package com.sessionintelligence.core;

import java.time.Instant;

public record WindowAggregate(
        SessionKey sessionKey,
        long requestCount,
        Instant lastSeen,
        String lastFingerprint
) {
}
