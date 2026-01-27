package com.sessionintelligence.core;

public interface RiskScorer {
    SessionRiskScore score(
            DetectionContext context,
            java.util.List<DetectorFinding> findings
    );
}
