package com.sessionintelligence.starter;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sessionintelligence.core.Fingerprint;
import com.sessionintelligence.core.ObservationStore;
import com.sessionintelligence.core.RequestObservation;
import com.sessionintelligence.core.SessionKey;
import com.sessionintelligence.core.SessionRiskScore;
import com.sessionintelligence.core.SessionSnapshot;
import com.sessionintelligence.core.SnapshotUpdate;
import com.sessionintelligence.core.WindowSnapshot;

public class InMemoryObservationStore implements ObservationStore {
    private final ConcurrentMap<String, SessionSnapshot> sessionSnapshots = new ConcurrentHashMap<>();
    private final ConcurrentMap<SessionKey, WindowSnapshot> windowSnapshots = new ConcurrentHashMap<>();
    private final ConcurrentMap<SessionKey, SessionRiskScore> riskScores = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Set<String>> windowNamesBySession = new ConcurrentHashMap<>();

    @Override
    public SnapshotUpdate<SessionSnapshot> recordSession(
            RequestObservation observation,
            Fingerprint fingerprint
    ) {
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
        SessionSnapshot previous = sessionSnapshots.get(sessionId);
        SessionSnapshot current = sessionSnapshots.compute(sessionId, (id, existing) -> {
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

    @Override
    public SnapshotUpdate<WindowSnapshot> recordWindow(
            RequestObservation observation,
            Fingerprint fingerprint
    ) {
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
        WindowSnapshot previous = windowSnapshots.get(key);
        WindowSnapshot current = windowSnapshots.compute(key, (ignored, existing) -> {
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

    @Override
    public void save(SessionRiskScore score) {
        if (score == null || score.sessionKey() == null) {
            return;
        }
        SessionKey key = score.sessionKey();
        if (key.sessionId() == null || key.sessionId().isBlank()) {
            return;
        }
        riskScores.put(key, score);
    }
}
