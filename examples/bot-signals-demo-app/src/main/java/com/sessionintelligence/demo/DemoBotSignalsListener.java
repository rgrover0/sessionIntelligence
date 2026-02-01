package com.sessionintelligence.demo;

import org.springframework.stereotype.Component;

import com.sessionintelligence.botsignals.AnomalyEvent;
import com.sessionintelligence.botsignals.BotSignalsListener;
import com.sessionintelligence.botsignals.SessionRiskScore;

@Component
public class DemoBotSignalsListener implements BotSignalsListener {
    private final SignalStore store;

    public DemoBotSignalsListener(SignalStore store) {
        this.store = store;
    }

    @Override
    public void onRiskScoreUpdated(SessionRiskScore score) {
        store.recordScore(score);
    }

    @Override
    public void onAnomalyDetected(AnomalyEvent event) {
        store.recordAnomaly(event);
    }
}
