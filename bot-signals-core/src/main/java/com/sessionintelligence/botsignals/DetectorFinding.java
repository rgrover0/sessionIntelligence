package com.sessionintelligence.botsignals;

import java.time.Instant;

public record DetectorFinding(
        Severity severity,
        ReasonCode reasonCode,
        EvidenceSummary evidenceSummary,
        Instant detectedAt
) {
}
