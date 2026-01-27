package com.sessionintelligence.starter.integration;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.sessionintelligence.core.AnomalyEvent;
import com.sessionintelligence.core.Fingerprint;
import com.sessionintelligence.core.ObservationStore;
import com.sessionintelligence.core.RequestObservation;
import com.sessionintelligence.core.SessionIntelligenceListener;
import com.sessionintelligence.core.SessionKey;
import com.sessionintelligence.core.SessionRiskScore;
import com.sessionintelligence.core.SessionSnapshot;
import com.sessionintelligence.core.SnapshotUpdate;
import com.sessionintelligence.core.WindowSnapshot;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class MultiPodIntegrationTest {
    private static final String INFINISPAN_IMAGE = "quay.io/infinispan/server:15.0";

    @Container
    static final GenericContainer<?> infinispan = new GenericContainer<>(INFINISPAN_IMAGE)
            .withExposedPorts(11222)
            .withEnv("USER", "admin")
            .withEnv("PASS", "password");

    private final SharedObservationStore sharedStore = SharedObservationStore.INSTANCE;
    private final SharedListener sharedListener = SharedListener.INSTANCE;

    @AfterEach
    void resetSharedState() {
        sharedStore.reset();
        sharedListener.reset();
    }

    @Test
    void storeContinuityAcrossInstances() {
        assertThat(infinispan.isRunning()).isTrue();
        ConfigurableApplicationContext contextA = startContext();
        ConfigurableApplicationContext contextB = startContext();
        try {
            TestRestTemplate clientA = createRestTemplate();
            TestRestTemplate clientB = createRestTemplate();
            int portA = getPort(contextA);
            int portB = getPort(contextB);
            String cookie = startSession(clientA, portA, "tab-1", "AgentA/1");
            callRate(clientB, portB, cookie, "tab-1", "AgentA/1");

            SessionSnapshot snapshot = sharedStore.getSessionSnapshot(sharedStore.lastSessionId());
            assertThat(snapshot).isNotNull();
            assertThat(snapshot.requestCount()).isEqualTo(2);
            assertThat(sharedStore.sessionCount()).isEqualTo(1);
        } finally {
            contextA.close();
            contextB.close();
        }
    }

    @Test
    void resurrectionDetectionFiresOnMismatch() {
        assertThat(infinispan.isRunning()).isTrue();
        ConfigurableApplicationContext contextA = startContext();
        ConfigurableApplicationContext contextB = startContext();
        try {
            TestRestTemplate clientA = createRestTemplate();
            TestRestTemplate clientB = createRestTemplate();
            int portA = getPort(contextA);
            int portB = getPort(contextB);
            String cookie = startSession(clientA, portA, "tab-1", "AgentA/1");
            String sessionId = sharedStore.lastSessionId();
            SessionSnapshot downgraded = new SessionSnapshot(
                    sessionId,
                    0,
                    1,
                    Instant.now().minusSeconds(60),
                    Instant.now().minusSeconds(30),
                    "hash"
            );
            sharedStore.forceSessionSnapshot(sessionId, downgraded);
            callRate(clientB, portB, cookie, "tab-1", "AgentA/1");

            assertThat(sharedListener.anomalies())
                    .anyMatch(event -> event.reasonCode().name().equals("SESSION_RESURRECTION"));
        } finally {
            contextA.close();
            contextB.close();
        }
    }

    @Test
    void collisionDetectionFiresOnFingerprintChange() {
        assertThat(infinispan.isRunning()).isTrue();
        ConfigurableApplicationContext contextA = startContext();
        ConfigurableApplicationContext contextB = startContext();
        try {
            TestRestTemplate clientA = createRestTemplate();
            TestRestTemplate clientB = createRestTemplate();
            int portA = getPort(contextA);
            int portB = getPort(contextB);
            String cookie = startSession(clientA, portA, "tab-1", "AgentA/1");
            callRate(clientB, portB, cookie, "tab-1", "AgentB/2");

            assertThat(sharedListener.anomalies())
                    .anyMatch(event -> event.reasonCode().name().equals("WINDOW_COLLISION"));
        } finally {
            contextA.close();
            contextB.close();
        }
    }

    private ConfigurableApplicationContext startContext() {
        return new SpringApplicationBuilder(TestApplication.class)
                .web(WebApplicationType.SERVLET)
                .properties(
                        "server.port=0",
                        "session-intelligence.enabled=true",
                        "session-intelligence.telemetry.publish-application-events=false"
                )
                .run();
    }

    private int getPort(ConfigurableApplicationContext context) {
        ServletWebServerApplicationContext serverContext = (ServletWebServerApplicationContext) context;
        WebServer server = serverContext.getWebServer();
        return server.getPort();
    }

    private TestRestTemplate createRestTemplate() {
        RestTemplateBuilder builder = new RestTemplateBuilder();
        return new TestRestTemplate(builder);
    }

    private String startSession(TestRestTemplate client, int port, String windowName, String userAgent) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Window-Name", windowName);
        headers.add("User-Agent", userAgent);
        ResponseEntity<String> response = client.exchange(
                "http://localhost:" + port + "/start",
                org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        String setCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).isNotNull();
        return setCookie.split(";", 2)[0];
    }

    private void callRate(
            TestRestTemplate client,
            int port,
            String cookie,
            String windowName,
            String userAgent
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, cookie);
        headers.add("X-Window-Name", windowName);
        headers.add("User-Agent", userAgent);
        client.exchange(
                "http://localhost:" + port + "/rate",
                org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
    }

    @EnableAutoConfiguration
    @RestController
    static class TestApplication {
        @GetMapping("/start")
        public String start(HttpServletRequest request) {
            request.getSession(true);
            return "ok";
        }

        @GetMapping("/rate")
        public String rate(@RequestHeader(name = "X-Window-Name", required = false) String windowName) {
            return windowName == null ? "missing" : "ok";
        }

        @Bean
        @Primary
        ObservationStore sharedObservationStore() {
            return SharedObservationStore.INSTANCE;
        }

        @Bean
        @Primary
        SessionIntelligenceListener sharedListener() {
            return SharedListener.INSTANCE;
        }
    }

    static final class SharedListener implements SessionIntelligenceListener {
        static final SharedListener INSTANCE = new SharedListener();
        private final List<AnomalyEvent> anomalies = new CopyOnWriteArrayList<>();
        private final List<SessionRiskScore> scores = new CopyOnWriteArrayList<>();

        @Override
        public void onRiskScoreUpdated(SessionRiskScore score) {
            scores.add(score);
        }

        @Override
        public void onAnomalyDetected(AnomalyEvent event) {
            anomalies.add(event);
        }

        List<AnomalyEvent> anomalies() {
            return anomalies;
        }

        void reset() {
            anomalies.clear();
            scores.clear();
        }
    }

    static final class SharedObservationStore implements ObservationStore {
        static final SharedObservationStore INSTANCE = new SharedObservationStore();
        private final ConcurrentMap<String, SessionSnapshot> sessionSnapshots = new ConcurrentHashMap<>();
        private final ConcurrentMap<SessionKey, WindowSnapshot> windowSnapshots = new ConcurrentHashMap<>();
        private final ConcurrentMap<SessionKey, SessionRiskScore> riskScores = new ConcurrentHashMap<>();
        private final ConcurrentMap<String, Set<String>> windowNames = new ConcurrentHashMap<>();
        private volatile String lastSessionId;

        @Override
        public SnapshotUpdate<SessionSnapshot> recordSession(
                RequestObservation observation,
                Fingerprint fingerprint
        ) {
            if (observation == null || observation.sessionKey() == null) {
                return null;
            }
            SessionKey key = observation.sessionKey();
            if (key.sessionId() == null) {
                return null;
            }
            lastSessionId = key.sessionId();
            Set<String> names = windowNames.computeIfAbsent(key.sessionId(), ignored -> ConcurrentHashMap.newKeySet());
            if (key.windowName() != null) {
                names.add(key.windowName());
            }
            SessionSnapshot previous = sessionSnapshots.get(key.sessionId());
            SessionSnapshot current = sessionSnapshots.compute(key.sessionId(), (id, existing) -> {
                long nextCount = existing == null ? 1L : existing.requestCount() + 1L;
                String lastFingerprint = fingerprint != null ? fingerprint.currentHash() : null;
                Instant now = observation.timestamp();
                return new SessionSnapshot(
                        id,
                        nextCount,
                        names.size(),
                        existing == null ? now : existing.firstSeen(),
                        now,
                        lastFingerprint
                );
            });
            return new SnapshotUpdate<>(previous, current);
        }

        @Override
        public SnapshotUpdate<WindowSnapshot> recordWindow(
                RequestObservation observation,
                Fingerprint fingerprint
        ) {
            if (observation == null || observation.sessionKey() == null) {
                return null;
            }
            SessionKey key = observation.sessionKey();
            if (key.sessionId() == null || key.windowName() == null) {
                return null;
            }
            WindowSnapshot previous = windowSnapshots.get(key);
            WindowSnapshot current = windowSnapshots.compute(key, (ignored, existing) -> {
                long nextCount = existing == null ? 1L : existing.requestCount() + 1L;
                String lastFingerprint = fingerprint != null ? fingerprint.currentHash() : null;
                Instant now = observation.timestamp();
                return new WindowSnapshot(
                        key,
                        nextCount,
                        existing == null ? now : existing.firstSeen(),
                        now,
                        lastFingerprint
                );
            });
            return new SnapshotUpdate<>(previous, current);
        }

        @Override
        public void save(SessionRiskScore score) {
            if (score == null || score.sessionKey() == null) {
                return;
            }
            riskScores.put(score.sessionKey(), score);
        }

        void forceSessionSnapshot(String sessionId, SessionSnapshot snapshot) {
            sessionSnapshots.put(sessionId, snapshot);
        }

        SessionSnapshot getSessionSnapshot(String sessionId) {
            return sessionSnapshots.get(sessionId);
        }

        int sessionCount() {
            return sessionSnapshots.size();
        }

        String lastSessionId() {
            return lastSessionId;
        }

        void reset() {
            sessionSnapshots.clear();
            windowSnapshots.clear();
            riskScores.clear();
            windowNames.clear();
            lastSessionId = null;
        }
    }
}
