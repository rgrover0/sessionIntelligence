package com.sessionintelligence.starter;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.sessionintelligence.core.FingerprintStrategy;
import com.sessionintelligence.core.ObservationDetector;
import com.sessionintelligence.core.ObservationStore;
import com.sessionintelligence.core.RequestObservation;
import com.sessionintelligence.core.SessionIntelligenceEngine;
import com.sessionintelligence.core.SessionKey;

import static org.assertj.core.api.Assertions.assertThat;

class PerformanceBudgetTest {

    @Test
    void observeStaysUnderBudget() {
        SessionIntelligenceProperties properties = new SessionIntelligenceProperties();
        ObservationStore store = new InMemoryObservationStore(properties);
        FingerprintStrategy fingerprintStrategy = new DefaultFingerprintStrategy(properties);
        List<ObservationDetector> detectors = List.of(
                new FingerprintDriftDetector(properties),
                new RequestRateDetector(properties, null),
                new WindowExplosionDetector(properties),
                new WindowCollisionDetector(properties),
                new SessionResurrectionDetector()
        );
        SessionIntelligenceEngine engine = new SessionIntelligenceEngine(
                store,
                fingerprintStrategy,
                List.of(new WeightedRiskScorer(properties)),
                detectors,
                List.of()
        );
        RequestObservation observation = new RequestObservation(
                Instant.now(),
                "GET",
                "/rate",
                200,
                null,
                "iphash",
                null,
                "uahash",
                "TestAgent",
                "1",
                "en-US",
                "gzip",
                Set.of("user-agent"),
                null,
                false,
                new SessionKey("perf-session", "perf-window")
        );

        int iterations = 1000;
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            engine.observe(observation);
        }
        long elapsed = System.nanoTime() - start;
        double averageMillis = elapsed / 1_000_000.0d / iterations;

        assertThat(averageMillis).isLessThan(5.0d);
    }

    @Test
    void capsWindowNamesPerSession() {
        SessionIntelligenceProperties properties = new SessionIntelligenceProperties();
        properties.getSafety().setMaxWindowNamesTracked(2);
        InMemoryObservationStore store = new InMemoryObservationStore(properties);
        Instant now = Instant.now();
        for (int i = 0; i < 5; i++) {
            RequestObservation observation = new RequestObservation(
                    now,
                    "GET",
                    "/rate",
                    200,
                    null,
                    "iphash",
                    null,
                    "uahash",
                    "TestAgent",
                    "1",
                    "en-US",
                    "gzip",
                    Set.of("user-agent"),
                    null,
                    false,
                    new SessionKey("cap-session", "window-" + i)
            );
            store.recordSession(observation, null);
        }
        RequestObservation observation = new RequestObservation(
                now,
                "GET",
                "/rate",
                200,
                null,
                "iphash",
                null,
                "uahash",
                "TestAgent",
                "1",
                "en-US",
                "gzip",
                Set.of("user-agent"),
                null,
                false,
                new SessionKey("cap-session", "window-final")
        );
        assertThat(store.recordSession(observation, null).current().windowCount())
                .isLessThanOrEqualTo(2);
    }
}
