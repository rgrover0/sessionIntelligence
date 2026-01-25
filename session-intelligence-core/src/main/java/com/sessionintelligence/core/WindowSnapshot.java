package com.sessionintelligence.core;

import java.time.Instant;

public record WindowSnapshot(
        SessionKey sessionKey,
        long requestCount,
        Instant firstSeen,
        Instant lastSeen,
        String lastFingerprint
) {
}
