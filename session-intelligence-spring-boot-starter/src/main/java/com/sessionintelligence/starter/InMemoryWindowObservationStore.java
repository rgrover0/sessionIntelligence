package com.sessionintelligence.starter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sessionintelligence.core.Fingerprint;
import com.sessionintelligence.core.RequestObservation;
import com.sessionintelligence.core.SessionKey;
import com.sessionintelligence.core.WindowAggregate;
import com.sessionintelligence.core.WindowObservationStore;

public class InMemoryWindowObservationStore implements WindowObservationStore {
    private final ConcurrentMap<SessionKey, WindowAggregate> aggregates = new ConcurrentHashMap<>();

    @Override
    public void record(RequestObservation observation, Fingerprint fingerprint) {
        if (observation == null || observation.sessionKey() == null) {
            return;
        }
        SessionKey key = observation.sessionKey();
        if (key.sessionId() == null || key.sessionId().isBlank()) {
            return;
        }
        if (key.windowName() == null || key.windowName().isBlank()) {
            return;
        }
        aggregates.compute(key, (ignored, existing) -> {
            long nextCount = existing == null ? 1L : existing.requestCount() + 1L;
            String currentFingerprint = fingerprint != null ? fingerprint.currentHash() : null;
            return new WindowAggregate(key, nextCount, observation.timestamp(), currentFingerprint);
        });
    }
}
