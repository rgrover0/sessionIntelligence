package com.sessionintelligence.starter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sessionintelligence.core.Fingerprint;
import com.sessionintelligence.core.RequestObservation;
import com.sessionintelligence.core.SessionKey;
import com.sessionintelligence.core.SessionObservationStore;
import com.sessionintelligence.core.SessionSnapshot;

public class InMemorySessionObservationStore implements SessionObservationStore {
    private final ConcurrentMap<String, SessionSnapshot> snapshots = new ConcurrentHashMap<>();

    @Override
    public SessionSnapshot record(RequestObservation observation, Fingerprint fingerprint) {
        if (observation == null || observation.sessionKey() == null) {
            return null;
        }
        SessionKey key = observation.sessionKey();
        if (key.sessionId() == null || key.sessionId().isBlank()) {
            return null;
        }
        return snapshots.compute(key.sessionId(), (id, existing) -> {
            long nextCount = existing == null ? 1L : existing.requestCount() + 1L;
            String currentFingerprint = fingerprint != null ? fingerprint.currentHash() : null;
            return new SessionSnapshot(
                    id,
                    nextCount,
                    existing == null ? observation.timestamp() : existing.firstSeen(),
                    observation.timestamp(),
                    currentFingerprint
            );
        });
    }
}
