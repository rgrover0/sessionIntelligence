package com.sessionintelligence.botsignals;

import java.util.List;

public interface RiskScorer {
    SessionRiskScore score(DetectionContext context, List<DetectorFinding> findings);
}
