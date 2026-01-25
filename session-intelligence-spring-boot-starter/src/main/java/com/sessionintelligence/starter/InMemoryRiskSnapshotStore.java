package com.sessionintelligence.starter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sessionintelligence.core.RiskSnapshot;
import com.sessionintelligence.core.RiskSnapshotStore;
import com.sessionintelligence.core.SessionKey;

public class InMemoryRiskSnapshotStore implements RiskSnapshotStore {
    private final ConcurrentMap<SessionKey, RiskSnapshot> snapshots = new ConcurrentHashMap<>();

    @Override
    public void save(RiskSnapshot snapshot) {
        if (snapshot == null || snapshot.sessionKey() == null) {
            return;
        }
        SessionKey key = snapshot.sessionKey();
        if (key.sessionId() == null || key.sessionId().isBlank()) {
            return;
        }
        snapshots.put(key, snapshot);
    }
}
