package com.sessionintelligence.starter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sessionintelligence.core.Fingerprint;
import com.sessionintelligence.core.RequestObservation;
import com.sessionintelligence.core.SessionAggregate;
import com.sessionintelligence.core.SessionKey;
import com.sessionintelligence.core.SessionObservationStore;

public class InMemorySessionObservationStore implements SessionObservationStore {
    private final ConcurrentMap<String, SessionAggregate> aggregates = new ConcurrentHashMap<>();

    @Override
    public void record(RequestObservation observation, Fingerprint fingerprint) {
        if (observation == null || observation.sessionKey() == null) {
            return;
        }
        SessionKey key = observation.sessionKey();
        if (key.sessionId() == null || key.sessionId().isBlank()) {
            return;
        }
        aggregates.compute(key.sessionId(), (id, existing) -> {
            long nextCount = existing == null ? 1L : existing.requestCount() + 1L;
            String currentFingerprint = fingerprint != null ? fingerprint.currentHash() : null;
            return new SessionAggregate(id, nextCount, observation.timestamp(), currentFingerprint);
        });
    }
}
