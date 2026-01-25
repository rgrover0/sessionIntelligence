package com.sessionintelligence.starter;

import com.sessionintelligence.core.EvidenceSummary;
import com.sessionintelligence.core.ReasonCode;
import com.sessionintelligence.core.RequestObservation;
import com.sessionintelligence.core.RiskScorer;
import com.sessionintelligence.core.SessionRiskScore;
import com.sessionintelligence.core.SessionSnapshot;
import com.sessionintelligence.core.WindowSnapshot;

import java.util.List;

public class NoOpRiskScorer implements RiskScorer {
    @Override
    public SessionRiskScore score(
            RequestObservation observation,
            SessionSnapshot sessionSnapshot,
            WindowSnapshot windowSnapshot
    ) {
        return new SessionRiskScore(
                observation.sessionKey(),
                0,
                List.of(ReasonCode.NONE),
                EvidenceSummary.from(sessionSnapshot, windowSnapshot),
                observation.timestamp()
        );
    }
}
