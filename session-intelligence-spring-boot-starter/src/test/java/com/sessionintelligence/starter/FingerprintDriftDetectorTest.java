package com.sessionintelligence.starter;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sessionintelligence.core.DetectionContext;
import com.sessionintelligence.core.DetectorFinding;
import com.sessionintelligence.core.Fingerprint;
import com.sessionintelligence.core.SessionKey;
import com.sessionintelligence.core.SessionSnapshot;

import static org.assertj.core.api.Assertions.assertThat;

class FingerprintDriftDetectorTest {

    @Test
    void normalTrafficDoesNotFlag() {
        SessionIntelligenceProperties properties = new SessionIntelligenceProperties();
        FingerprintDriftDetector detector = new FingerprintDriftDetector(properties);
        Instant now = Instant.now();
        SessionSnapshot previous = new SessionSnapshot("s1", 1, 1, now.minusSeconds(10), now.minusSeconds(10), "hash-a");
        SessionSnapshot current = new SessionSnapshot("s1", 2, 1, previous.firstSeen(), now, "hash-a");
        DetectionContext context = DetectorTestSupport.context(
                DetectorTestSupport.observation(now, "/rate", new SessionKey("s1", "w1")),
                previous,
                current,
                null,
                null,
                new Fingerprint("hash-a", List.of())
        );

        List<DetectorFinding> findings = detector.detect(context);
        assertThat(findings).isEmpty();
    }

    @Test
    void botLikeTrafficFlagsDrift() {
        SessionIntelligenceProperties properties = new SessionIntelligenceProperties();
        FingerprintDriftDetector detector = new FingerprintDriftDetector(properties);
        Instant now = Instant.now();
        SessionSnapshot previous = new SessionSnapshot("s1", 1, 1, now.minusSeconds(10), now.minusSeconds(10), "hash-a");
        SessionSnapshot current = new SessionSnapshot("s1", 2, 1, previous.firstSeen(), now, "hash-b");
        DetectionContext context = DetectorTestSupport.context(
                DetectorTestSupport.observation(now, "/rate", new SessionKey("s1", "w1")),
                previous,
                current,
                null,
                null,
                new Fingerprint("hash-b", List.of("hash-a"))
        );

        List<DetectorFinding> findings = detector.detect(context);
        assertThat(findings).hasSize(1);
    }

    @Test
    void borderlineOutsideWindowDoesNotFlag() {
        SessionIntelligenceProperties properties = new SessionIntelligenceProperties();
        properties.getThresholds().setFingerprintDriftWindow(Duration.ofSeconds(1));
        FingerprintDriftDetector detector = new FingerprintDriftDetector(properties);
        Instant now = Instant.now();
        SessionSnapshot previous = new SessionSnapshot("s1", 1, 1, now.minusSeconds(10), now.minusSeconds(10), "hash-a");
        SessionSnapshot current = new SessionSnapshot("s1", 2, 1, previous.firstSeen(), now, "hash-b");
        DetectionContext context = DetectorTestSupport.context(
                DetectorTestSupport.observation(now, "/rate", new SessionKey("s1", "w1")),
                previous,
                current,
                null,
                null,
                new Fingerprint("hash-b", List.of())
        );

        List<DetectorFinding> findings = detector.detect(context);
        assertThat(findings).isEmpty();
    }
}
