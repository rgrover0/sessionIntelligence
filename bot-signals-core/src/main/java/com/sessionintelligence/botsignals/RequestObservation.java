package com.sessionintelligence.botsignals;

import java.time.Instant;
import java.util.Set;

import com.sessionintelligence.windowsession.SessionKey;

public record RequestObservation(
        Instant timestamp,
        String method,
        String path,
        int statusCode,
        String clientIp,
        String clientIpHash,
        String userAgent,
        String userAgentHash,
        String userAgentFamily,
        String userAgentMajor,
        String acceptLanguage,
        String acceptEncoding,
        Set<String> headerNames,
        String principalName,
        boolean authenticated,
        SessionKey sessionKey
) {
}
