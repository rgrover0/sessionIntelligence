package com.sessionintelligence.starter;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sessionintelligence.core.Fingerprint;
import com.sessionintelligence.core.ObservationStore;
import com.sessionintelligence.core.RequestObservation;
import com.sessionintelligence.core.SessionRiskScore;
import com.sessionintelligence.core.SessionSnapshot;
import com.sessionintelligence.core.SnapshotUpdate;
import com.sessionintelligence.core.WindowSnapshot;

public class FailOpenObservationStore implements ObservationStore {
    private static final Logger log = LoggerFactory.getLogger(FailOpenObservationStore.class);

    private final ObservationStore delegate;
    private final int failureThreshold;
    private final Duration openDuration;
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private volatile Instant openUntil;

    public FailOpenObservationStore(
            ObservationStore delegate,
            SessionIntelligenceProperties properties
    ) {
        this.delegate = delegate;
        this.failureThreshold = Math.max(1, properties.getSafety().getCircuitBreakerFailureThreshold());
        this.openDuration = properties.getSafety().getCircuitBreakerOpenDuration();
    }

    @Override
    public SnapshotUpdate<SessionSnapshot> recordSession(
            RequestObservation observation,
            Fingerprint fingerprint
    ) {
        if (isOpen()) {
            return null;
        }
        try {
            SnapshotUpdate<SessionSnapshot> update = delegate.recordSession(observation, fingerprint);
            resetFailures();
            return update;
        } catch (Exception ex) {
            recordFailure(ex);
            return null;
        }
    }

    @Override
    public SnapshotUpdate<WindowSnapshot> recordWindow(
            RequestObservation observation,
            Fingerprint fingerprint
    ) {
        if (isOpen()) {
            return null;
        }
        try {
            SnapshotUpdate<WindowSnapshot> update = delegate.recordWindow(observation, fingerprint);
            resetFailures();
            return update;
        } catch (Exception ex) {
            recordFailure(ex);
            return null;
        }
    }

    @Override
    public void save(SessionRiskScore score) {
        if (isOpen()) {
            return;
        }
        try {
            delegate.save(score);
            resetFailures();
        } catch (Exception ex) {
            recordFailure(ex);
        }
    }

    private boolean isOpen() {
        Instant until = openUntil;
        if (until == null) {
            return false;
        }
        if (Instant.now().isAfter(until)) {
            openUntil = null;
            consecutiveFailures.set(0);
            return false;
        }
        return true;
    }

    private void recordFailure(Exception ex) {
        int failures = consecutiveFailures.incrementAndGet();
        if (failures >= failureThreshold) {
            Duration duration = openDuration;
            if (duration == null || duration.isZero() || duration.isNegative()) {
                duration = Duration.ofSeconds(30);
            }
            openUntil = Instant.now().plus(duration);
            log.warn("Session intelligence store circuit opened for {}s", duration.getSeconds(), ex);
        } else {
            log.debug("Session intelligence store failure {}", failures, ex);
        }
    }

    private void resetFailures() {
        consecutiveFailures.set(0);
    }
}
