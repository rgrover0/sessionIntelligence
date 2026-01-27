package com.sessionintelligence.store.infinispan;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import com.sessionintelligence.core.ObservationStore;
import com.sessionintelligence.starter.SessionIntelligenceProperties;

@AutoConfiguration
public class InfinispanObservationStoreAutoConfiguration {

    @Bean(name = "sessionIntelligenceObservationStore")
    @ConditionalOnMissingBean(name = "sessionIntelligenceObservationStore")
    @ConditionalOnProperty(
            prefix = "session-intelligence.storage",
            name = "backend",
            havingValue = "INFINISPAN_REMOTE"
    )
    public ObservationStore sessionIntelligenceObservationStore(SessionIntelligenceProperties properties) {
        return new InfinispanRemoteObservationStore(properties);
    }
}
