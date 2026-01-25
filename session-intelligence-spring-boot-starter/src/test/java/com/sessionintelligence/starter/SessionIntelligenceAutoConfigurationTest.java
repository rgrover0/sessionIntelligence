package com.sessionintelligence.starter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.sessionintelligence.core.FingerprintStrategy;
import com.sessionintelligence.core.RiskSnapshotStore;
import com.sessionintelligence.core.SessionIntelligenceEngine;
import com.sessionintelligence.core.SessionIntelligenceListener;
import com.sessionintelligence.core.SessionObservationStore;
import com.sessionintelligence.core.WindowObservationStore;

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
            assertThat(context).hasSingleBean(SessionObservationStore.class);
            assertThat(context).hasSingleBean(WindowObservationStore.class);
            assertThat(context).hasSingleBean(RiskSnapshotStore.class);
            assertThat(context).hasSingleBean(FingerprintStrategy.class);
            assertThat(context).hasSingleBean(SessionIntelligenceListener.class);
        });
    }
}
