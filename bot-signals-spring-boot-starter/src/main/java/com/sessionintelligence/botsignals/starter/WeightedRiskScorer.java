package com.sessionintelligence.botsignals.starter;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sessionintelligence.botsignals.DetectionContext;
import com.sessionintelligence.botsignals.DetectorFinding;
import com.sessionintelligence.botsignals.EvidenceSummary;
import com.sessionintelligence.botsignals.ReasonCode;
import com.sessionintelligence.botsignals.RiskScorer;
import com.sessionintelligence.botsignals.SessionRiskScore;
import com.sessionintelligence.botsignals.Severity;

public class WeightedRiskScorer implements RiskScorer {
    private final BotSignalsProperties properties;

    public WeightedRiskScorer(BotSignalsProperties properties) {
        this.properties = properties;
    }

    @Override
    public SessionRiskScore score(DetectionContext context, List<DetectorFinding> findings) {
        if (context == null || context.observation() == null) {
            return null;
        }
        Instant now = context.observation().timestamp();
        EvidenceSummary evidence = EvidenceSummary.from(
                context.sessionUpdate() != null ? context.sessionUpdate().current() : null,
                context.windowUpdate() != null ? context.windowUpdate().current() : null
        );
        if (findings == null || findings.isEmpty()) {
            return new SessionRiskScore(
                    context.observation().sessionKey(),
                    0,
                    List.of(ReasonCode.NONE),
                    evidence,
                    now
            );
        }
        BotSignalsProperties.Scoring scoring = properties.getScoring();
        Map<ReasonCode, Integer> weights = scoring.getWeights();
        Map<Severity, Double> multipliers = scoring.getSeverityMultipliers();
        int defaultWeight = scoring.getDefaultWeight();
        double total = 0.0d;
        List<ReasonCode> reasons = new ArrayList<>();
        for (DetectorFinding finding : findings) {
            if (finding == null) {
                continue;
            }
            ReasonCode reason = finding.reasonCode();
            if (reason != null && !reasons.contains(reason)) {
                reasons.add(reason);
            }
            int baseWeight = weights != null && reason != null
                    ? weights.getOrDefault(reason, defaultWeight)
                    : defaultWeight;
            double multiplier = multipliers != null && finding.severity() != null
                    ? multipliers.getOrDefault(finding.severity(), 1.0d)
                    : 1.0d;
            double decay = decayFactor(scoring.getDecayWindow(), finding.detectedAt(), now);
            total += baseWeight * multiplier * decay;
        }
        if (reasons.isEmpty()) {
            reasons = List.of(ReasonCode.NONE);
        }
        int maxScore = scoring.getMaxScore() > 0 ? scoring.getMaxScore() : 100;
        int score = (int) Math.round(Math.min(total, maxScore));
        return new SessionRiskScore(
                context.observation().sessionKey(),
                score,
                reasons,
                evidence,
                now
        );
    }

    private double decayFactor(Duration window, Instant detectedAt, Instant now) {
        if (window == null || window.isZero() || window.isNegative()) {
            return 1.0d;
        }
        if (detectedAt == null || now == null) {
            return 1.0d;
        }
        long ageMillis = Math.max(0L, Duration.between(detectedAt, now).toMillis());
        long windowMillis = window.toMillis();
        if (windowMillis <= 0L) {
            return 1.0d;
        }
        if (ageMillis >= windowMillis) {
            return 0.0d;
        }
        return 1.0d - (ageMillis / (double) windowMillis);
    }
}
