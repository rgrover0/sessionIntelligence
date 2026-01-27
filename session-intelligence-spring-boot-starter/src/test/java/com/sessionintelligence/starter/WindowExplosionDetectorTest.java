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

class WindowExplosionDetectorTest {

    @Test
    void normalTrafficDoesNotFlag() {
        SessionIntelligenceProperties properties = new SessionIntelligenceProperties();
        WindowExplosionDetector detector = new WindowExplosionDetector(properties);
        Instant now = Instant.now();
        SessionSnapshot snapshot = new SessionSnapshot("s1", 5, 3, now.minusSeconds(30), now.minusSeconds(5), "hash-a");
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
    void botLikeTrafficFlagsExplosion() {
        SessionIntelligenceProperties properties = new SessionIntelligenceProperties();
        properties.getThresholds().setMaxParallelWindows(2);
        properties.getThresholds().setWindowExplosionWindow(Duration.ofMinutes(2));
        WindowExplosionDetector detector = new WindowExplosionDetector(properties);
        Instant now = Instant.now();
        SessionSnapshot snapshot = new SessionSnapshot("s1", 10, 5, now.minusSeconds(20), now.minusSeconds(5), "hash-a");
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
    void borderlineOutsideWindowDoesNotFlag() {
        SessionIntelligenceProperties properties = new SessionIntelligenceProperties();
        properties.getThresholds().setMaxParallelWindows(2);
        properties.getThresholds().setWindowExplosionWindow(Duration.ofSeconds(1));
        WindowExplosionDetector detector = new WindowExplosionDetector(properties);
        Instant now = Instant.now();
        SessionSnapshot snapshot = new SessionSnapshot("s1", 10, 5, now.minusSeconds(10), now.minusSeconds(5), "hash-a");
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
}
