package com.sessionintelligence.botsignals;

import java.time.Instant;

import com.sessionintelligence.windowsession.SessionKey;

public record WindowSnapshot(
        SessionKey sessionKey,
        long requestCount,
        Instant firstSeen,
        Instant lastSeen,
        String lastFingerprint
) {
}
