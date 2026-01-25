package com.sessionintelligence.starter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sessionintelligence.core.SessionKey;
import com.sessionintelligence.core.SessionRiskScore;
import com.sessionintelligence.core.SessionRiskScoreStore;

public class InMemorySessionRiskScoreStore implements SessionRiskScoreStore {
    private final ConcurrentMap<SessionKey, SessionRiskScore> scores = new ConcurrentHashMap<>();

    @Override
    public void save(SessionRiskScore score) {
        if (score == null || score.sessionKey() == null) {
            return;
        }
        SessionKey key = score.sessionKey();
        if (key.sessionId() == null || key.sessionId().isBlank()) {
            return;
        }
        scores.put(key, score);
    }
}
