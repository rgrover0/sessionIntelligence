package com.sessionintelligence.starter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.sessionintelligence.core.ObservationStore;
import com.sessionintelligence.core.SessionIntelligenceEngine;

import static org.assertj.core.api.Assertions.assertThat;

class SessionIntelligenceAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SessionIntelligenceAutoConfiguration.class))
            .withPropertyValues("session-intelligence.enabled=true");

    @Test
    void autoConfigurationLoads() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(SessionIntelligenceEngine.class);
            assertThat(context).hasSingleBean(SessionIntelligenceFilter.class);
            assertThat(context).hasSingleBean(ObservationStore.class);
        });
    }
}
