package com.sessionintelligence.botsignals.starter;

import java.util.List;

import com.sessionintelligence.botsignals.DetectionContext;
import com.sessionintelligence.botsignals.DetectorFinding;
import com.sessionintelligence.botsignals.EvidenceSummary;
import com.sessionintelligence.botsignals.ReasonCode;
import com.sessionintelligence.botsignals.RiskScorer;
import com.sessionintelligence.botsignals.SessionRiskScore;

public class NoOpRiskScorer implements RiskScorer {
    @Override
    public SessionRiskScore score(DetectionContext context, List<DetectorFinding> findings) {
        if (context == null || context.observation() == null) {
            return null;
        }
        return new SessionRiskScore(
                context.observation().sessionKey(),
                0,
                List.of(ReasonCode.NONE),
                EvidenceSummary.from(
                        context.sessionUpdate() != null ? context.sessionUpdate().current() : null,
                        context.windowUpdate() != null ? context.windowUpdate().current() : null
                ),
                context.observation().timestamp()
        );
    }
}
