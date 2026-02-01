package com.sessionintelligence.core;

import java.time.Instant;

public record RequestObservation(
        Instant timestamp,
        String method,
        String path,
        int statusCode,
        String clientIp,
        String userAgent,
        SessionKey sessionKey
) {
}
