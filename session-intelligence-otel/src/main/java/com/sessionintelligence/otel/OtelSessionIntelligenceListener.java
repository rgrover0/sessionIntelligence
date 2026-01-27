package com.sessionintelligence.otel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.api.trace.Span;

import com.sessionintelligence.core.AnomalyEvent;
import com.sessionintelligence.core.SessionIntelligenceListener;
import com.sessionintelligence.core.SessionKey;
import com.sessionintelligence.core.SessionRiskScore;
import com.sessionintelligence.starter.SessionIntelligenceHasher;

public class OtelSessionIntelligenceListener implements SessionIntelligenceListener {
    private static final Logger log = LoggerFactory.getLogger(OtelSessionIntelligenceListener.class);
    private final SessionIntelligenceHasher hasher;

    public OtelSessionIntelligenceListener(SessionIntelligenceHasher hasher) {
        this.hasher = hasher;
    }

    @Override
    public void onRiskScoreUpdated(SessionRiskScore score) {
        Span span = Span.current();
        if (!span.getSpanContext().isValid()) {
            return;
        }
        SessionKey key = score.sessionKey();
        span.setAttribute("session.id.hash", hashValue(key != null ? key.sessionId() : null));
        span.setAttribute("window.name.hash", hashValue(key != null ? key.windowName() : null));
        span.setAttribute("risk.score", score.score());
        span.setAttribute("risk.reasons", score.reasonCodes().toString());
        log.debug("session_intelligence otel risk_score exported");
    }

    @Override
    public void onAnomalyDetected(AnomalyEvent event) {
        Span span = Span.current();
        if (!span.getSpanContext().isValid()) {
            return;
        }
        SessionKey key = event.sessionKey();
        span.setAttribute("session.id.hash", hashValue(key != null ? key.sessionId() : null));
        span.setAttribute("window.name.hash", hashValue(key != null ? key.windowName() : null));
        span.setAttribute("anomaly.codes", event.reasonCode() != null ? event.reasonCode().name() : "unknown");
        log.debug("session_intelligence otel anomaly exported");
    }

    private String hashValue(String value) {
        if (hasher == null) {
            return null;
        }
        return hasher.hashValue(value);
    }
}
