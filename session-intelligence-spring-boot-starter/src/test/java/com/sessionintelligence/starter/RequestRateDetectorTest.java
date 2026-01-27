package com.sessionintelligence.starter;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sessionintelligence.core.DetectionContext;
import com.sessionintelligence.core.DetectorFinding;
import com.sessionintelligence.core.Fingerprint;
import com.sessionintelligence.core.SessionKey;
import com.sessionintelligence.core.SessionSnapshot;

import static org.assertj.core.api.Assertions.assertThat;

class RequestRateDetectorTest {

    @Test
    void normalTrafficDoesNotFlag() {
        SessionIntelligenceProperties properties = new SessionIntelligenceProperties();
        properties.getThresholds().setMaxRequestsPerMinute(120);
        RequestRateDetector detector = new RequestRateDetector(properties, null);
        Instant now = Instant.now();
        SessionSnapshot snapshot = new SessionSnapshot("s1", 10, 1, now.minusSeconds(60), now.minusSeconds(1), "hash-a");
        DetectionContext context = DetectorTestSupport.context(
                DetectorTestSupport.observation(now, "/rate", new SessionKey("s1", "w1")),
                snapshot,
                snapshot,
                null,
                null,
                new Fingerprint("hash-a", List.of())
        );

        List<DetectorFinding> findings = detector.detect(context);
        assertThat(findings).isEmpty();
    }

    @Test
    void botLikeTrafficFlagsRate() {
        SessionIntelligenceProperties properties = new SessionIntelligenceProperties();
        properties.getThresholds().setMaxRequestsPerMinute(120);
        RequestRateDetector detector = new RequestRateDetector(properties, null);
        Instant now = Instant.now();
        SessionSnapshot snapshot = new SessionSnapshot("s1", 500, 1, now.minusSeconds(60), now.minusSeconds(1), "hash-a");
        DetectionContext context = DetectorTestSupport.context(
                DetectorTestSupport.observation(now, "/rate", new SessionKey("s1", "w1")),
                snapshot,
                snapshot,
                null,
                null,
                new Fingerprint("hash-a", List.of())
        );

        List<DetectorFinding> findings = detector.detect(context);
        assertThat(findings).hasSize(1);
    }

    @Test
    void borderlineEqualThresholdDoesNotFlag() {
        SessionIntelligenceProperties properties = new SessionIntelligenceProperties();
        properties.getThresholds().setMaxRequestsPerMinute(120);
        RequestRateDetector detector = new RequestRateDetector(properties, null);
        Instant now = Instant.now();
        SessionSnapshot snapshot = new SessionSnapshot("s1", 120, 1, now.minusSeconds(60), now.minusSeconds(1), "hash-a");
        DetectionContext context = DetectorTestSupport.context(
                DetectorTestSupport.observation(now, "/rate", new SessionKey("s1", "w1")),
                snapshot,
                snapshot,
                null,
                null,
                new Fingerprint("hash-a", List.of())
        );

        List<DetectorFinding> findings = detector.detect(context);
        assertThat(findings).isEmpty();
    }

    @Test
    void nonRateEndpointDoesNotFlag() {
        SessionIntelligenceProperties properties = new SessionIntelligenceProperties();
        properties.getThresholds().setRateEndpointPatterns(List.of("/rate/**"));
        RequestRateDetector detector = new RequestRateDetector(properties, null);
        Instant now = Instant.now();
        SessionSnapshot snapshot = new SessionSnapshot("s1", 500, 1, now.minusSeconds(60), now.minusSeconds(1), "hash-a");
        DetectionContext context = DetectorTestSupport.context(
                DetectorTestSupport.observation(now, "/other", new SessionKey("s1", "w1")),
                snapshot,
                snapshot,
                null,
                null,
                new Fingerprint("hash-a", List.of())
        );

        List<DetectorFinding> findings = detector.detect(context);
        assertThat(findings).isEmpty();
    }
}
