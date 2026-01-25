package com.sessionintelligence.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import com.sessionintelligence.core.AnomalyEvent;
import com.sessionintelligence.core.RequestObservation;
import com.sessionintelligence.core.SessionIntelligenceListener;
import com.sessionintelligence.core.SessionKey;

public class PublishingSessionIntelligenceListener implements SessionIntelligenceListener {
    private static final Logger log = LoggerFactory.getLogger(PublishingSessionIntelligenceListener.class);

    private final ApplicationEventPublisher publisher;

    public PublishingSessionIntelligenceListener(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void onObservation(RequestObservation observation) {
        publisher.publishEvent(observation);
        SessionKey key = observation.sessionKey();
        log.debug(
                "session-intelligence observation sessionId={} windowName={} status={}",
                key != null ? key.sessionId() : null,
                key != null ? key.windowName() : null,
                observation.statusCode()
        );
    }

    @Override
    public void onAnomaly(AnomalyEvent event) {
        publisher.publishEvent(event);
        SessionKey key = event.sessionKey();
        log.warn(
                "session-intelligence anomaly type={} sessionId={} windowName={}",
                event.type(),
                key != null ? key.sessionId() : null,
                key != null ? key.windowName() : null
        );
    }
}
