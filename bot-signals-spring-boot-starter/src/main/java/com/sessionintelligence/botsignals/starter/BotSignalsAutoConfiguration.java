package com.sessionintelligence.botsignals.starter;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import io.micrometer.core.instrument.MeterRegistry;

import com.sessionintelligence.botsignals.BotSignalsEngine;
import com.sessionintelligence.botsignals.BotSignalsListener;
import com.sessionintelligence.botsignals.FingerprintStrategy;
import com.sessionintelligence.botsignals.ObservationDetector;
import com.sessionintelligence.botsignals.ObservationStore;
import com.sessionintelligence.botsignals.RiskScorer;
import com.sessionintelligence.windowsession.WindowScopedAttributeAccessor;
import com.sessionintelligence.windowsession.WindowSessionKeyResolver;

@AutoConfiguration
@EnableConfigurationProperties(BotSignalsProperties.class)
@ConditionalOnProperty(
        prefix = "bot-signals",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class BotSignalsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public BotSignalsHasher botSignalsHasher(BotSignalsProperties properties) {
        return new BotSignalsHasher(properties);
    }

    @Bean
    @ConditionalOnClass(MeterRegistry.class)
    @ConditionalOnBean(MeterRegistry.class)
    @ConditionalOnProperty(
            prefix = "bot-signals.telemetry",
            name = "metrics-enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public BotSignalsMetrics botSignalsMetrics(MeterRegistry registry) {
        return new BotSignalsMetrics(registry);
    }

    @Bean
    @ConditionalOnMissingBean
    public FingerprintStrategy fingerprintStrategy(BotSignalsProperties properties) {
        return new DefaultFingerprintStrategy(properties);
    }

    @Bean(name = "botSignalsObservationStore")
    @ConditionalOnMissingBean(name = "botSignalsObservationStore")
    @ConditionalOnProperty(
            prefix = "bot-signals.storage",
            name = "backend",
            havingValue = "IN_MEMORY",
            matchIfMissing = true
    )
    public ObservationStore botSignalsObservationStore(BotSignalsProperties properties) {
        return new InMemoryObservationStore(properties);
    }

    @Bean(name = "botSignalsObservationStore")
    @ConditionalOnMissingBean(name = "botSignalsObservationStore")
    @ConditionalOnProperty(
            prefix = "bot-signals.storage",
            name = "backend",
            havingValue = "REDIS"
    )
    public ObservationStore botSignalsObservationStoreRedis() {
        return new RedisObservationStore();
    }

    @Bean
    @Primary
    @ConditionalOnProperty(
            prefix = "bot-signals.safety",
            name = "fail-open-enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public ObservationStore failOpenObservationStore(
            @Qualifier("botSignalsObservationStore") ObservationStore delegate,
            BotSignalsProperties properties
    ) {
        return new FailOpenObservationStore(delegate, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "bot-signals.features",
            name = "risk-scoring",
            havingValue = "true",
            matchIfMissing = true
    )
    public RiskScorer riskScorer(BotSignalsProperties properties) {
        return new WeightedRiskScorer(properties);
    }

    @Bean
    @ConditionalOnMissingBean(FingerprintDriftDetector.class)
    @ConditionalOnProperty(
            prefix = "bot-signals.features",
            name = "anomaly-detection",
            havingValue = "true",
            matchIfMissing = true
    )
    public FingerprintDriftDetector fingerprintDriftDetector(BotSignalsProperties properties) {
        return new FingerprintDriftDetector(properties);
    }

    @Bean
    @ConditionalOnMissingBean(RequestRateDetector.class)
    @ConditionalOnProperty(
            prefix = "bot-signals.features",
            name = "anomaly-detection",
            havingValue = "true",
            matchIfMissing = true
    )
    public RequestRateDetector requestRateDetector(
            BotSignalsProperties properties,
            ObjectProvider<BotSignalsMetrics> metricsProvider
    ) {
        return new RequestRateDetector(properties, metricsProvider.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean(WindowExplosionDetector.class)
    @ConditionalOnProperty(
            prefix = "bot-signals.features",
            name = "anomaly-detection",
            havingValue = "true",
            matchIfMissing = true
    )
    public WindowExplosionDetector windowExplosionDetector(BotSignalsProperties properties) {
        return new WindowExplosionDetector(properties);
    }

    @Bean
    @ConditionalOnMissingBean(WindowCollisionDetector.class)
    @ConditionalOnProperty(
            prefix = "bot-signals.features",
            name = "anomaly-detection",
            havingValue = "true",
            matchIfMissing = true
    )
    public WindowCollisionDetector windowCollisionDetector(BotSignalsProperties properties) {
        return new WindowCollisionDetector(properties);
    }

    @Bean
    @ConditionalOnMissingBean(SessionResurrectionDetector.class)
    @ConditionalOnProperty(
            prefix = "bot-signals.features",
            name = "anomaly-detection",
            havingValue = "true",
            matchIfMissing = true
    )
    public SessionResurrectionDetector sessionResurrectionDetector() {
        return new SessionResurrectionDetector();
    }

    @Bean
    @ConditionalOnMissingBean(BotSignalsListener.class)
    public BotSignalsListener botSignalsListener(
            ApplicationEventPublisher publisher,
            ObjectProvider<BotSignalsMetrics> metricsProvider,
            BotSignalsHasher hasher
    ) {
        return new PublishingBotSignalsListener(
                publisher,
                metricsProvider.getIfAvailable(),
                hasher
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public BotSignalsEngine botSignalsEngine(
            ObservationStore observationStore,
            FingerprintStrategy fingerprintStrategy,
            List<RiskScorer> riskScorers,
            List<ObservationDetector> detectors,
            List<BotSignalsListener> listeners
    ) {
        return new BotSignalsEngine(
                observationStore,
                fingerprintStrategy,
                riskScorers,
                detectors,
                listeners
        );
    }

    @Bean
    public BotSignalsFilter botSignalsFilter(
            BotSignalsEngine engine,
            BotSignalsProperties properties,
            BotSignalsHasher hasher,
            ObjectProvider<WindowSessionKeyResolver> keyResolverProvider,
            ObjectProvider<WindowScopedAttributeAccessor> accessorProvider
    ) {
        return new BotSignalsFilter(
                engine,
                properties,
                hasher,
                keyResolverProvider.getIfAvailable(),
                accessorProvider.getIfAvailable()
        );
    }
}
