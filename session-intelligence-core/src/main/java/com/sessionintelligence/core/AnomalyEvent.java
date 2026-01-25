package com.sessionintelligence.core;

import java.time.Instant;

public record AnomalyEvent(
        Instant timestamp,
        SessionKey sessionKey,
        String type,
        String message
) {
}
