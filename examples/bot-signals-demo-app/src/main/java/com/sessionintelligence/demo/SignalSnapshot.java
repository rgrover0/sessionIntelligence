package com.sessionintelligence.demo;

import java.util.List;

import com.sessionintelligence.botsignals.AnomalyEvent;
import com.sessionintelligence.botsignals.SessionRiskScore;

public record SignalSnapshot(
        String sessionId,
        SessionRiskScore riskScore,
        List<AnomalyEvent> anomalies
) {
}
