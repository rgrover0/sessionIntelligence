package com.sessionintelligence.starter;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.ApplicationEventPublisher;

import io.micrometer.core.instrument.MeterRegistry;

import com.sessionintelligence.core.FingerprintStrategy;
import com.sessionintelligence.core.ObservationStore;
import com.sessionintelligence.core.ObservationDetector;
import com.sessionintelligence.core.RiskScorer;
import com.sessionintelligence.core.SessionActionAdvisor;
import com.sessionintelligence.core.SessionIntelligenceEngine;
import com.sessionintelligence.core.SessionIntelligenceListener;

@AutoConfiguration
@EnableConfigurationProperties(SessionIntelligenceProperties.class)
@ConditionalOnProperty(
        prefix = "session-intelligence",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class SessionIntelligenceAutoConfiguration {

    @Bean(name = "sessionIntelligenceObservationStore")
    @ConditionalOnMissingBean(name = "sessionIntelligenceObservationStore")
    @ConditionalOnProperty(
            prefix = "session-intelligence.storage",
            name = "backend",
            havingValue = "IN_MEMORY",
            matchIfMissing = true
    )
    public ObservationStore sessionIntelligenceObservationStore(SessionIntelligenceProperties properties) {
        return new InMemoryObservationStore(properties);
    }

    @Bean(name = "sessionIntelligenceObservationStore")
    @ConditionalOnMissingBean(name = "sessionIntelligenceObservationStore")
    @ConditionalOnProperty(
            prefix = "session-intelligence.storage",
            name = "backend",
            havingValue = "REDIS"
    )
    public ObservationStore sessionIntelligenceObservationStoreRedis() {
        return new RedisObservationStore();
    }

    @Bean
    @Primary
    @ConditionalOnProperty(
            prefix = "session-intelligence.safety",
            name = "fail-open-enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public ObservationStore failOpenObservationStore(
            @Qualifier("sessionIntelligenceObservationStore") ObservationStore delegate,
            SessionIntelligenceProperties properties
    ) {
        return new FailOpenObservationStore(delegate, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionIntelligenceHasher sessionIntelligenceHasher(SessionIntelligenceProperties properties) {
        return new SessionIntelligenceHasher(properties);
    }

    @Bean
    @ConditionalOnClass(MeterRegistry.class)
    @ConditionalOnProperty(
            prefix = "session-intelligence.telemetry",
            name = "metrics-enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public SessionIntelligenceMetrics sessionIntelligenceMetrics(MeterRegistry registry) {
        return new SessionIntelligenceMetrics(registry);
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
            ApplicationEventPublisher publisher,
            ObjectProvider<SessionIntelligenceMetrics> metricsProvider,
            SessionIntelligenceHasher hasher
    ) {
        return new PublishingSessionIntelligenceListener(
                publisher,
                metricsProvider.getIfAvailable(),
                hasher
        );
    }

    @Bean
    @ConditionalOnMissingBean(SessionIntelligenceListener.class)
    public SessionIntelligenceListener fallbackSessionIntelligenceListener() {
        return new NoOpSessionIntelligenceListener();
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
    public RequestRateDetector requestRateDetector(
            SessionIntelligenceProperties properties,
            ObjectProvider<SessionIntelligenceMetrics> metricsProvider
    ) {
        return new RequestRateDetector(properties, metricsProvider.getIfAvailable());
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
            ObservationStore observationStore,
            FingerprintStrategy fingerprintStrategy,
            List<RiskScorer> riskScorers,
            List<ObservationDetector> detectors,
            List<SessionIntelligenceListener> listeners
    ) {
        return new SessionIntelligenceEngine(
                observationStore,
                fingerprintStrategy,
                riskScorers,
                detectors,
                listeners
        );
    }

    @Bean
    public SessionIntelligenceFilter sessionIntelligenceFilter(
            SessionIntelligenceEngine engine,
            SessionIntelligenceProperties properties,
            SessionIntelligenceHasher hasher
    ) {
        return new SessionIntelligenceFilter(engine, properties, hasher);
    }
}
