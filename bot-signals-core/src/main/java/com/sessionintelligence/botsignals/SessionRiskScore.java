package com.sessionintelligence.botsignals;

import java.time.Instant;
import java.util.List;

import com.sessionintelligence.windowsession.SessionKey;

public record SessionRiskScore(
        SessionKey sessionKey,
        int score,
        List<ReasonCode> reasonCodes,
        EvidenceSummary evidenceSummary,
        Instant updatedAt
) {
    public SessionRiskScore {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("score must be between 0 and 100");
        }
        if (reasonCodes == null) {
            reasonCodes = List.of();
        } else {
            reasonCodes = List.copyOf(reasonCodes);
        }
    }
}
