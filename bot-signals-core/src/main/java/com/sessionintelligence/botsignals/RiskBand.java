package com.sessionintelligence.botsignals;

public enum RiskBand {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public static RiskBand fromScore(int score) {
        if (score >= 81) {
            return CRITICAL;
        }
        if (score >= 51) {
            return HIGH;
        }
        if (score >= 21) {
            return MEDIUM;
        }
        return LOW;
    }
}
