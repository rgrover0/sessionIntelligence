package com.sessionintelligence.botsignals.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import com.sessionintelligence.botsignals.AnomalyEvent;
import com.sessionintelligence.botsignals.BotSignalsListener;
import com.sessionintelligence.botsignals.SessionRiskScore;
import com.sessionintelligence.windowsession.SessionKey;

public class PublishingBotSignalsListener implements BotSignalsListener {
    private static final Logger log = LoggerFactory.getLogger(PublishingBotSignalsListener.class);

    private final ApplicationEventPublisher publisher;
    private final BotSignalsMetrics metrics;
    private final BotSignalsHasher hasher;

    public PublishingBotSignalsListener(
            ApplicationEventPublisher publisher,
            BotSignalsMetrics metrics,
            BotSignalsHasher hasher
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
                "bot_signals risk_score session_hash={} window_hash={} score={} reasons={} window_count={}",
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
                "bot_signals anomaly session_hash={} window_hash={} reason={} severity={}",
                sessionHash,
                windowHash,
                event.reasonCode(),
                event.severity()
        );
    }
}
