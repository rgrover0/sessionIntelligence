package com.sessionintelligence.starter;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.sessionintelligence.core.ObservationStore;
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

    @Bean
    @ConditionalOnMissingBean
    public ObservationStore observationStore() {
        return new InMemoryObservationStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionIntelligenceListener sessionIntelligenceListener() {
        return new NoOpSessionIntelligenceListener();
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionIntelligenceEngine sessionIntelligenceEngine(
            ObservationStore observationStore,
            List<SessionIntelligenceListener> listeners
    ) {
        return new SessionIntelligenceEngine(observationStore, listeners);
    }

    @Bean
    public SessionIntelligenceFilter sessionIntelligenceFilter(
            SessionIntelligenceEngine engine,
            SessionIntelligenceProperties properties
    ) {
        return new SessionIntelligenceFilter(engine, properties);
    }
}
