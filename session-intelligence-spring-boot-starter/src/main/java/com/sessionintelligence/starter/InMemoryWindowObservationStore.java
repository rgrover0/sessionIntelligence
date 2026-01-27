package com.sessionintelligence.starter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sessionintelligence.core.Fingerprint;
import com.sessionintelligence.core.RequestObservation;
import com.sessionintelligence.core.SessionKey;
import com.sessionintelligence.core.WindowObservationStore;
import com.sessionintelligence.core.WindowSnapshot;
import com.sessionintelligence.core.SnapshotUpdate;

public class InMemoryWindowObservationStore implements WindowObservationStore {
    private final ConcurrentMap<SessionKey, WindowSnapshot> snapshots = new ConcurrentHashMap<>();

    @Override
    public SnapshotUpdate<WindowSnapshot> record(RequestObservation observation, Fingerprint fingerprint) {
        if (observation == null || observation.sessionKey() == null) {
            return null;
        }
        SessionKey key = observation.sessionKey();
        if (key.sessionId() == null || key.sessionId().isBlank()) {
            return null;
        }
        if (key.windowName() == null || key.windowName().isBlank()) {
            return null;
        }
        WindowSnapshot previous = snapshots.get(key);
        WindowSnapshot current = snapshots.compute(key, (ignored, existing) -> {
            long nextCount = existing == null ? 1L : existing.requestCount() + 1L;
            String currentFingerprint = fingerprint != null ? fingerprint.currentHash() : null;
            return new WindowSnapshot(
                    key,
                    nextCount,
                    existing == null ? observation.timestamp() : existing.firstSeen(),
                    observation.timestamp(),
                    currentFingerprint
            );
        });
        return new SnapshotUpdate<>(previous, current);
    }
}
