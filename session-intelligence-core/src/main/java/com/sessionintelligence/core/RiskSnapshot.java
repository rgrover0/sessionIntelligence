package com.sessionintelligence.core;

import java.time.Instant;

public record RiskSnapshot(
        Instant timestamp,
        SessionKey sessionKey,
        double score,
        String label
) {
}
