package com.sessionintelligence.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sessionintelligence.core.AnomalyEvent;
import com.sessionintelligence.core.SessionIntelligenceListener;
import com.sessionintelligence.core.SessionRiskScore;

public class OtelSessionIntelligenceListener implements SessionIntelligenceListener {
    private static final Logger log = LoggerFactory.getLogger(OtelSessionIntelligenceListener.class);

    @Override
    public void onRiskScoreUpdated(SessionRiskScore score) {
        log.debug("session-intelligence otel export risk-score sessionId={}",
                score.sessionKey() != null ? score.sessionKey().sessionId() : null);
    }

    @Override
    public void onAnomalyDetected(AnomalyEvent event) {
        log.debug("session-intelligence otel export anomaly type={}", event.type());
    }
}
