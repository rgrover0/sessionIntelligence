package com.sessionintelligence.starter;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sessionintelligence.core.DetectionContext;
import com.sessionintelligence.core.DetectorFinding;
import com.sessionintelligence.core.Fingerprint;
import com.sessionintelligence.core.SessionKey;
import com.sessionintelligence.core.WindowSnapshot;

import static org.assertj.core.api.Assertions.assertThat;

class WindowCollisionDetectorTest {

    @Test
    void normalTrafficDoesNotFlag() {
        SessionIntelligenceProperties properties = new SessionIntelligenceProperties();
        WindowCollisionDetector detector = new WindowCollisionDetector(properties);
        Instant now = Instant.now();
        SessionKey key = new SessionKey("s1", "w1");
        WindowSnapshot previous = new WindowSnapshot(key, 1, now.minusSeconds(10), now.minusSeconds(10), "hash-a");
        WindowSnapshot current = new WindowSnapshot(key, 2, previous.firstSeen(), now, "hash-a");
        DetectionContext context = DetectorTestSupport.context(
                DetectorTestSupport.observation(now, "/rate", key),
                null,
                null,
                previous,
                current,
                new Fingerprint("hash-a", List.of())
        );

        List<DetectorFinding> findings = detector.detect(context);
        assertThat(findings).isEmpty();
    }

    @Test
    void botLikeTrafficFlagsCollision() {
        SessionIntelligenceProperties properties = new SessionIntelligenceProperties();
        WindowCollisionDetector detector = new WindowCollisionDetector(properties);
        Instant now = Instant.now();
        SessionKey key = new SessionKey("s1", "w1");
        WindowSnapshot previous = new WindowSnapshot(key, 1, now.minusSeconds(5), now.minusSeconds(5), "hash-a");
        WindowSnapshot current = new WindowSnapshot(key, 2, previous.firstSeen(), now, "hash-b");
        DetectionContext context = DetectorTestSupport.context(
                DetectorTestSupport.observation(now, "/rate", key),
                null,
                null,
                previous,
                current,
                new Fingerprint("hash-b", List.of())
        );

        List<DetectorFinding> findings = detector.detect(context);
        assertThat(findings).hasSize(1);
    }

    @Test
    void borderlineOutsideWindowDoesNotFlag() {
        SessionIntelligenceProperties properties = new SessionIntelligenceProperties();
        properties.getThresholds().setFingerprintDriftWindow(Duration.ofSeconds(1));
        WindowCollisionDetector detector = new WindowCollisionDetector(properties);
        Instant now = Instant.now();
        SessionKey key = new SessionKey("s1", "w1");
        WindowSnapshot previous = new WindowSnapshot(key, 1, now.minusSeconds(10), now.minusSeconds(10), "hash-a");
        WindowSnapshot current = new WindowSnapshot(key, 2, previous.firstSeen(), now, "hash-b");
        DetectionContext context = DetectorTestSupport.context(
                DetectorTestSupport.observation(now, "/rate", key),
                null,
                null,
                previous,
                current,
                new Fingerprint("hash-b", List.of())
        );

        List<DetectorFinding> findings = detector.detect(context);
        assertThat(findings).isEmpty();
    }
}
