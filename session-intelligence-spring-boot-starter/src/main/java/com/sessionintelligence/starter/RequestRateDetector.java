package com.sessionintelligence.starter;

import java.time.Duration;
import java.util.List;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import com.sessionintelligence.core.DetectionContext;
import com.sessionintelligence.core.DetectorFinding;
import com.sessionintelligence.core.EvidenceSummary;
import com.sessionintelligence.core.ObservationDetector;
import com.sessionintelligence.core.ReasonCode;
import com.sessionintelligence.core.Severity;
import com.sessionintelligence.core.SessionSnapshot;

public class RequestRateDetector implements ObservationDetector {
    private final SessionIntelligenceProperties properties;
    private final PathMatcher pathMatcher = new AntPathMatcher();
    private final SessionIntelligenceMetrics metrics;

    public RequestRateDetector(
            SessionIntelligenceProperties properties,
            SessionIntelligenceMetrics metrics
    ) {
        this.properties = properties;
        this.metrics = metrics;
    }

    @Override
    public List<DetectorFinding> detect(DetectionContext context) {
        if (context == null || context.observation() == null) {
            return List.of();
        }
        String path = context.observation().path();
        if (!matchesRateEndpoints(path)) {
            return List.of();
        }
        SessionSnapshot snapshot = context.sessionUpdate() != null
                ? context.sessionUpdate().current()
                : null;
        if (snapshot == null || snapshot.firstSeen() == null) {
            return List.of();
        }
        int maxPerMinute = properties.getThresholds().getMaxRequestsPerMinute();
        Duration elapsed = Duration.between(snapshot.firstSeen(), context.observation().timestamp()).abs();
        double minutes = Math.max(1.0d, elapsed.toMillis() / 60_000.0d);
        double rate = snapshot.requestCount() / minutes;
        if (metrics != null) {
            metrics.recordRate("session", rate);
            if (context.windowUpdate() != null && context.windowUpdate().current() != null) {
                recordWindowRate(context.windowUpdate().current(), context.observation().timestamp());
            }
        }
        if (rate <= maxPerMinute) {
            return List.of();
        }
        Severity severity = rate >= maxPerMinute * 2.0d ? Severity.HIGH : Severity.MEDIUM;
        DetectorFinding finding = new DetectorFinding(
                severity,
                ReasonCode.HIGH_REQUEST_RATE,
                EvidenceSummary.from(
                        context.sessionUpdate() != null ? context.sessionUpdate().current() : null,
                        context.windowUpdate() != null ? context.windowUpdate().current() : null
                ),
                context.observation().timestamp()
        );
        return List.of(finding);
    }

    private void recordWindowRate(com.sessionintelligence.core.WindowSnapshot snapshot, java.time.Instant now) {
        if (snapshot == null || snapshot.firstSeen() == null || now == null) {
            return;
        }
        Duration elapsed = Duration.between(snapshot.firstSeen(), now).abs();
        double minutes = Math.max(1.0d, elapsed.toMillis() / 60_000.0d);
        double rate = snapshot.requestCount() / minutes;
        metrics.recordRate("window", rate);
    }

    private boolean matchesRateEndpoints(String path) {
        List<String> patterns = properties.getThresholds().getRateEndpointPatterns();
        if (patterns == null || patterns.isEmpty()) {
            return true;
        }
        for (String pattern : patterns) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }
}
