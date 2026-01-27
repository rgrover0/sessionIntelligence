package com.sessionintelligence.starter;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sessionintelligence.core.Fingerprint;
import com.sessionintelligence.core.RequestObservation;
import com.sessionintelligence.core.SessionKey;
import com.sessionintelligence.core.SessionObservationStore;
import com.sessionintelligence.core.SessionSnapshot;
import com.sessionintelligence.core.SnapshotUpdate;

public class InMemorySessionObservationStore implements SessionObservationStore {
    private final ConcurrentMap<String, SessionSnapshot> snapshots = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Set<String>> windowNamesBySession = new ConcurrentHashMap<>();

    @Override
    public SnapshotUpdate<SessionSnapshot> record(RequestObservation observation, Fingerprint fingerprint) {
        if (observation == null || observation.sessionKey() == null) {
            return null;
        }
        SessionKey key = observation.sessionKey();
        if (key.sessionId() == null || key.sessionId().isBlank()) {
            return null;
        }
        String sessionId = key.sessionId();
        Set<String> windowNames = windowNamesBySession.computeIfAbsent(
                sessionId,
                ignored -> ConcurrentHashMap.newKeySet()
        );
        String windowName = key.windowName();
        if (windowName != null && !windowName.isBlank()) {
            windowNames.add(windowName);
        }
        SessionSnapshot previous = snapshots.get(sessionId);
        SessionSnapshot current = snapshots.compute(sessionId, (id, existing) -> {
            long nextCount = existing == null ? 1L : existing.requestCount() + 1L;
            String currentFingerprint = fingerprint != null ? fingerprint.currentHash() : null;
            return new SessionSnapshot(
                    id,
                    nextCount,
                    windowNames.size(),
                    existing == null ? observation.timestamp() : existing.firstSeen(),
                    observation.timestamp(),
                    currentFingerprint
            );
        });
        return new SnapshotUpdate<>(previous, current);
    }
}
