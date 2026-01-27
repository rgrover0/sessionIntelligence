package com.sessionintelligence.starter;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
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
    private final SessionIntelligenceProperties properties;

    public InMemoryObservationStore(SessionIntelligenceProperties properties) {
        this.properties = properties;
    }

    @Override
    public SnapshotUpdate<SessionSnapshot> recordSession(
            RequestObservation observation,
            Fingerprint fingerprint
    ) {
        if (observation == null || observation.sessionKey() == null) {
            return null;
        }
        Instant now = observation.timestamp() != null ? observation.timestamp() : Instant.now();
        pruneExpired(now);
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
        int maxTracked = properties.getSafety().getMaxWindowNamesTracked();
        if (windowName != null && !windowName.isBlank()) {
            if (maxTracked <= 0 || windowNames.size() < maxTracked) {
                windowNames.add(windowName);
            }
        }
        SessionSnapshot previous = sessionSnapshots.get(sessionId);
        int windowCount = maxTracked > 0 ? Math.min(windowNames.size(), maxTracked) : windowNames.size();
        SessionSnapshot current = sessionSnapshots.compute(sessionId, (id, existing) -> {
            long nextCount = existing == null ? 1L : existing.requestCount() + 1L;
            String currentFingerprint = fingerprint != null ? fingerprint.currentHash() : null;
            return new SessionSnapshot(
                    id,
                    nextCount,
                    windowCount,
                    existing == null ? now : existing.firstSeen(),
                    now,
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
        Instant now = observation.timestamp() != null ? observation.timestamp() : Instant.now();
        pruneExpired(now);
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
                    existing == null ? now : existing.firstSeen(),
                    now,
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
        Instant now = score.updatedAt() != null ? score.updatedAt() : Instant.now();
        pruneExpired(now);
        SessionKey key = score.sessionKey();
        if (key.sessionId() == null || key.sessionId().isBlank()) {
            return;
        }
        riskScores.put(key, score);
    }

    private void pruneExpired(Instant now) {
        Duration ttl = properties.getPrivacy().getRetentionTtl();
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            return;
        }
        Instant cutoff = now.minus(ttl);
        pruneSessionSnapshots(cutoff);
        pruneWindowSnapshots(cutoff);
        pruneRiskScores(cutoff);
    }

    private void pruneSessionSnapshots(Instant cutoff) {
        for (Iterator<Map.Entry<String, SessionSnapshot>> it = sessionSnapshots.entrySet().iterator();
                it.hasNext(); ) {
            Map.Entry<String, SessionSnapshot> entry = it.next();
            SessionSnapshot snapshot = entry.getValue();
            if (snapshot == null || snapshot.lastSeen() == null || snapshot.lastSeen().isBefore(cutoff)) {
                it.remove();
                windowNamesBySession.remove(entry.getKey());
            }
        }
    }

    private void pruneWindowSnapshots(Instant cutoff) {
        for (Iterator<Map.Entry<SessionKey, WindowSnapshot>> it = windowSnapshots.entrySet().iterator();
                it.hasNext(); ) {
            Map.Entry<SessionKey, WindowSnapshot> entry = it.next();
            WindowSnapshot snapshot = entry.getValue();
            if (snapshot == null || snapshot.lastSeen() == null || snapshot.lastSeen().isBefore(cutoff)) {
                it.remove();
            }
        }
    }

    private void pruneRiskScores(Instant cutoff) {
        for (Iterator<Map.Entry<SessionKey, SessionRiskScore>> it = riskScores.entrySet().iterator();
                it.hasNext(); ) {
            Map.Entry<SessionKey, SessionRiskScore> entry = it.next();
            SessionRiskScore score = entry.getValue();
            if (score == null || score.updatedAt() == null || score.updatedAt().isBefore(cutoff)) {
                it.remove();
            }
        }
    }
}
