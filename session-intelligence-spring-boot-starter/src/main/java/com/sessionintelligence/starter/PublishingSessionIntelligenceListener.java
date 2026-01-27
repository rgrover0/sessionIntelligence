package com.sessionintelligence.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import com.sessionintelligence.core.AnomalyEvent;
import com.sessionintelligence.core.SessionIntelligenceListener;
import com.sessionintelligence.core.SessionKey;
import com.sessionintelligence.core.SessionRiskScore;

public class PublishingSessionIntelligenceListener implements SessionIntelligenceListener {
    private static final Logger log = LoggerFactory.getLogger(PublishingSessionIntelligenceListener.class);

    private final ApplicationEventPublisher publisher;

    public PublishingSessionIntelligenceListener(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void onRiskScoreUpdated(SessionRiskScore score) {
        publisher.publishEvent(score);
        SessionKey key = score.sessionKey();
        log.info(
                "session-intelligence risk-score sessionId={} windowName={} score={} reasons={}",
                key != null ? key.sessionId() : null,
                key != null ? key.windowName() : null,
                score.score(),
                score.reasonCodes()
        );
    }

    @Override
    public void onAnomalyDetected(AnomalyEvent event) {
        publisher.publishEvent(event);
        SessionKey key = event.sessionKey();
        log.warn(
                "session-intelligence anomaly reason={} severity={} sessionId={} windowName={}",
                event.reasonCode(),
                event.severity(),
                key != null ? key.sessionId() : null,
                key != null ? key.windowName() : null
        );
    }
}
