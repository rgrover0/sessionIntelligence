package com.sessionintelligence.starter;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.ApplicationEventPublisher;

import com.sessionintelligence.core.FingerprintStrategy;
import com.sessionintelligence.core.ObservationDetector;
import com.sessionintelligence.core.RiskScorer;
import com.sessionintelligence.core.SessionActionAdvisor;
import com.sessionintelligence.core.SessionIntelligenceEngine;
import com.sessionintelligence.core.SessionIntelligenceListener;
import com.sessionintelligence.core.SessionObservationStore;
import com.sessionintelligence.core.SessionRiskScoreStore;
import com.sessionintelligence.core.WindowObservationStore;

@AutoConfiguration
@EnableConfigurationProperties(SessionIntelligenceProperties.class)
@ConditionalOnProperty(
        prefix = "session-intelligence",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class SessionIntelligenceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "session-intelligence.storage",
            name = "backend",
            havingValue = "IN_MEMORY",
            matchIfMissing = true
    )
    public SessionObservationStore sessionObservationStore() {
        return new InMemorySessionObservationStore();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "session-intelligence.storage",
            name = "backend",
            havingValue = "IN_MEMORY",
            matchIfMissing = true
    )
    public WindowObservationStore windowObservationStore() {
        return new InMemoryWindowObservationStore();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "session-intelligence.storage",
            name = "backend",
            havingValue = "IN_MEMORY",
            matchIfMissing = true
    )
    public SessionRiskScoreStore sessionRiskScoreStore() {
        return new InMemorySessionRiskScoreStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public FingerprintStrategy fingerprintStrategy(SessionIntelligenceProperties properties) {
        return new DefaultFingerprintStrategy(properties);
    }

    @Bean
    @ConditionalOnMissingBean(SessionIntelligenceListener.class)
    @ConditionalOnProperty(
            prefix = "session-intelligence.telemetry",
            name = "publish-application-events",
            havingValue = "true",
            matchIfMissing = true
    )
    public SessionIntelligenceListener sessionIntelligenceListener(
            ApplicationEventPublisher publisher
    ) {
        return new PublishingSessionIntelligenceListener(publisher);
    }

    @Bean
    @ConditionalOnMissingBean(SessionIntelligenceListener.class)
    public SessionIntelligenceListener fallbackSessionIntelligenceListener() {
        return new NoOpSessionIntelligenceListener();
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "session-intelligence.telemetry",
            name = "otel-enabled",
            havingValue = "true"
    )
    @ConditionalOnClass(name = "io.opentelemetry.api.OpenTelemetry")
    public SessionIntelligenceListener otelSessionIntelligenceListener() {
        return new OtelSessionIntelligenceListener();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "session-intelligence.features",
            name = "action-advisory",
            havingValue = "true",
            matchIfMissing = true
    )
    public SessionActionAdvisor sessionActionAdvisor() {
        return new NoOpSessionActionAdvisor();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "session-intelligence.features",
            name = "risk-scoring",
            havingValue = "true",
            matchIfMissing = true
    )
    public RiskScorer riskScorer(SessionIntelligenceProperties properties) {
        return new WeightedRiskScorer(properties);
    }

    @Bean
    @ConditionalOnMissingBean(FingerprintDriftDetector.class)
    @ConditionalOnProperty(
            prefix = "session-intelligence.features",
            name = "anomaly-detection",
            havingValue = "true",
            matchIfMissing = true
    )
    public FingerprintDriftDetector fingerprintDriftDetector(SessionIntelligenceProperties properties) {
        return new FingerprintDriftDetector(properties);
    }

    @Bean
    @ConditionalOnMissingBean(RequestRateDetector.class)
    @ConditionalOnProperty(
            prefix = "session-intelligence.features",
            name = "anomaly-detection",
            havingValue = "true",
            matchIfMissing = true
    )
    public RequestRateDetector requestRateDetector(SessionIntelligenceProperties properties) {
        return new RequestRateDetector(properties);
    }

    @Bean
    @ConditionalOnMissingBean(WindowExplosionDetector.class)
    @ConditionalOnProperty(
            prefix = "session-intelligence.features",
            name = "anomaly-detection",
            havingValue = "true",
            matchIfMissing = true
    )
    public WindowExplosionDetector windowExplosionDetector(SessionIntelligenceProperties properties) {
        return new WindowExplosionDetector(properties);
    }

    @Bean
    @ConditionalOnMissingBean(WindowCollisionDetector.class)
    @ConditionalOnProperty(
            prefix = "session-intelligence.features",
            name = "anomaly-detection",
            havingValue = "true",
            matchIfMissing = true
    )
    public WindowCollisionDetector windowCollisionDetector(SessionIntelligenceProperties properties) {
        return new WindowCollisionDetector(properties);
    }

    @Bean
    @ConditionalOnMissingBean(SessionResurrectionDetector.class)
    @ConditionalOnProperty(
            prefix = "session-intelligence.features",
            name = "anomaly-detection",
            havingValue = "true",
            matchIfMissing = true
    )
    public SessionResurrectionDetector sessionResurrectionDetector() {
        return new SessionResurrectionDetector();
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionIntelligenceEngine sessionIntelligenceEngine(
            SessionObservationStore sessionObservationStore,
            WindowObservationStore windowObservationStore,
            SessionRiskScoreStore riskScoreStore,
            FingerprintStrategy fingerprintStrategy,
            List<RiskScorer> riskScorers,
            List<ObservationDetector> detectors,
            List<SessionIntelligenceListener> listeners
    ) {
        return new SessionIntelligenceEngine(
                sessionObservationStore,
                windowObservationStore,
                riskScoreStore,
                fingerprintStrategy,
                riskScorers,
                detectors,
                listeners
        );
    }

    @Bean
    public SessionIntelligenceFilter sessionIntelligenceFilter(
            SessionIntelligenceEngine engine,
            SessionIntelligenceProperties properties
    ) {
        return new SessionIntelligenceFilter(engine, properties);
    }
}
