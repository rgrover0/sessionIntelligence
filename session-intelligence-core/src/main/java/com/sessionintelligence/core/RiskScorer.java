package com.sessionintelligence.core;

public interface RiskScorer {
    SessionRiskScore score(
            RequestObservation observation,
            SessionSnapshot sessionSnapshot,
            WindowSnapshot windowSnapshot
    );
}
