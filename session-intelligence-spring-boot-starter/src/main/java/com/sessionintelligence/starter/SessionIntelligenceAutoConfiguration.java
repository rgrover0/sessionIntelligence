package com.sessionintelligence.starter;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.ApplicationEventPublisher;

import com.sessionintelligence.core.FingerprintStrategy;
import com.sessionintelligence.core.RiskSnapshotStore;
import com.sessionintelligence.core.SessionIntelligenceEngine;
import com.sessionintelligence.core.SessionIntelligenceListener;
import com.sessionintelligence.core.SessionObservationStore;
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
    public SessionObservationStore sessionObservationStore() {
        return new InMemorySessionObservationStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public WindowObservationStore windowObservationStore() {
        return new InMemoryWindowObservationStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public RiskSnapshotStore riskSnapshotStore() {
        return new InMemoryRiskSnapshotStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public FingerprintStrategy fingerprintStrategy(SessionIntelligenceProperties properties) {
        return new DefaultFingerprintStrategy(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionIntelligenceListener sessionIntelligenceListener(
            ApplicationEventPublisher publisher
    ) {
        return new PublishingSessionIntelligenceListener(publisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionIntelligenceEngine sessionIntelligenceEngine(
            SessionObservationStore sessionObservationStore,
            WindowObservationStore windowObservationStore,
            RiskSnapshotStore riskSnapshotStore,
            FingerprintStrategy fingerprintStrategy,
            List<SessionIntelligenceListener> listeners
    ) {
        return new SessionIntelligenceEngine(
                sessionObservationStore,
                windowObservationStore,
                riskSnapshotStore,
                fingerprintStrategy,
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
