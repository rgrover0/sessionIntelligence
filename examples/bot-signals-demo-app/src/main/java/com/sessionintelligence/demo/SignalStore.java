package com.sessionintelligence.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

import com.sessionintelligence.botsignals.AnomalyEvent;
import com.sessionintelligence.botsignals.SessionRiskScore;

@Component
public class SignalStore {
    private final ConcurrentMap<String, SessionRiskScore> latestScores = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, List<AnomalyEvent>> anomalies = new ConcurrentHashMap<>();

    public void recordScore(SessionRiskScore score) {
        if (score == null || score.sessionKey() == null || score.sessionKey().sessionId() == null) {
            return;
        }
        latestScores.put(score.sessionKey().sessionId(), score);
    }

    public void recordAnomaly(AnomalyEvent event) {
        if (event == null || event.sessionKey() == null || event.sessionKey().sessionId() == null) {
            return;
        }
        String sessionId = event.sessionKey().sessionId();
        anomalies.computeIfAbsent(sessionId, key -> new CopyOnWriteArrayList<>()).add(event);
    }

    public SignalSnapshot snapshot(String sessionId) {
        SessionRiskScore score = latestScores.get(sessionId);
        List<AnomalyEvent> events = anomalies.getOrDefault(sessionId, List.of());
        return new SignalSnapshot(sessionId, score, new ArrayList<>(events));
    }
}
