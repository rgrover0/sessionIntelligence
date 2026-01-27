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
    private final SessionIntelligenceMetrics metrics;
    private final SessionIntelligenceHasher hasher;

    public PublishingSessionIntelligenceListener(
            ApplicationEventPublisher publisher,
            SessionIntelligenceMetrics metrics,
            SessionIntelligenceHasher hasher
    ) {
        this.publisher = publisher;
        this.metrics = metrics;
        this.hasher = hasher;
    }

    @Override
    public void onRiskScoreUpdated(SessionRiskScore score) {
        publisher.publishEvent(score);
        if (metrics != null) {
            metrics.recordRiskScore(score);
            metrics.recordWindowCount(score.evidenceSummary());
        }
        SessionKey key = score.sessionKey();
        String sessionHash = hasher != null && key != null ? hasher.hashValue(key.sessionId()) : null;
        String windowHash = hasher != null && key != null ? hasher.hashValue(key.windowName()) : null;
        log.info(
                "session_intelligence risk_score session_hash={} window_hash={} score={} reasons={} window_count={}",
                sessionHash,
                windowHash,
                score.score(),
                score.reasonCodes(),
                score.evidenceSummary() != null ? score.evidenceSummary().sessionWindowCount() : null
        );
    }

    @Override
    public void onAnomalyDetected(AnomalyEvent event) {
        publisher.publishEvent(event);
        if (metrics != null) {
            metrics.recordAnomaly(event);
        }
        SessionKey key = event.sessionKey();
        String sessionHash = hasher != null && key != null ? hasher.hashValue(key.sessionId()) : null;
        String windowHash = hasher != null && key != null ? hasher.hashValue(key.windowName()) : null;
        log.warn(
                "session_intelligence anomaly session_hash={} window_hash={} reason={} severity={}",
                sessionHash,
                windowHash,
                event.reasonCode(),
                event.severity()
        );
    }
}
