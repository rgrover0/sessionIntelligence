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

class SessionResurrectionDetectorTest {

    @Test
    void normalTrafficDoesNotFlag() {
        SessionResurrectionDetector detector = new SessionResurrectionDetector();
        Instant now = Instant.now();
        SessionSnapshot previous = new SessionSnapshot("s1", 3, 1, now.minusSeconds(20), now.minusSeconds(10), "hash-a");
        SessionSnapshot current = new SessionSnapshot("s1", 4, 1, previous.firstSeen(), now.minusSeconds(1), "hash-a");
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
    void botLikeTrafficFlagsResurrection() {
        SessionResurrectionDetector detector = new SessionResurrectionDetector();
        Instant now = Instant.now();
        SessionSnapshot previous = new SessionSnapshot("s1", 10, 1, now.minusSeconds(20), now.minusSeconds(5), "hash-a");
        SessionSnapshot current = new SessionSnapshot("s1", 2, 1, previous.firstSeen(), now.minusSeconds(1), "hash-a");
        DetectionContext context = DetectorTestSupport.context(
                DetectorTestSupport.observation(now, "/rate", new SessionKey("s1", "w1")),
                previous,
                current,
                null,
                null,
                new Fingerprint("hash-a", List.of())
        );

        List<DetectorFinding> findings = detector.detect(context);
        assertThat(findings).hasSize(1);
    }

    @Test
    void borderlineInconsistentTimestampsFlags() {
        SessionResurrectionDetector detector = new SessionResurrectionDetector();
        Instant now = Instant.now();
        SessionSnapshot previous = new SessionSnapshot("s1", 1, 1, now.minusSeconds(5), now.minusSeconds(3), "hash-a");
        SessionSnapshot current = new SessionSnapshot("s1", 2, 1, now, now.minusSeconds(1), "hash-a");
        DetectionContext context = DetectorTestSupport.context(
                DetectorTestSupport.observation(now, "/rate", new SessionKey("s1", "w1")),
                previous,
                current,
                null,
                null,
                new Fingerprint("hash-a", List.of())
        );

        List<DetectorFinding> findings = detector.detect(context);
        assertThat(findings).hasSize(1);
    }
}
