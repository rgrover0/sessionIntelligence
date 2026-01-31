package com.sessionintelligence.botsignals;

import java.time.Instant;

import com.sessionintelligence.windowsession.SessionKey;

public record AnomalyEvent(
        Instant timestamp,
        SessionKey sessionKey,
        Severity severity,
        ReasonCode reasonCode,
        EvidenceSummary evidenceSummary,
        String message
) {
}
