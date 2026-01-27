package com.sessionintelligence.starter;

import com.sessionintelligence.core.DetectionContext;
import com.sessionintelligence.core.EvidenceSummary;
import com.sessionintelligence.core.ReasonCode;
import com.sessionintelligence.core.RiskScorer;
import com.sessionintelligence.core.SessionRiskScore;

import java.util.List;

public class NoOpRiskScorer implements RiskScorer {
    @Override
    public SessionRiskScore score(
            DetectionContext context,
            List<com.sessionintelligence.core.DetectorFinding> findings
    ) {
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
