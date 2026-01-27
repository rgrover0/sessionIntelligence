package com.sessionintelligence.starter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.sessionintelligence.core.FingerprintStrategy;
import com.sessionintelligence.core.ObservationDetector;
import com.sessionintelligence.core.ObservationStore;
import com.sessionintelligence.core.RiskScorer;
import com.sessionintelligence.core.SessionActionAdvisor;
import com.sessionintelligence.core.SessionIntelligenceEngine;
import com.sessionintelligence.core.SessionIntelligenceListener;

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
            assertThat(context).hasBean("sessionIntelligenceObservationStore");
            assertThat(context.getBean(ObservationStore.class)).isInstanceOf(FailOpenObservationStore.class);
            assertThat(context).hasSingleBean(FingerprintStrategy.class);
            assertThat(context).hasSingleBean(RiskScorer.class);
            assertThat(context).hasBean("fingerprintDriftDetector");
            assertThat(context).hasBean("requestRateDetector");
            assertThat(context).hasBean("windowExplosionDetector");
            assertThat(context).hasBean("windowCollisionDetector");
            assertThat(context).hasBean("sessionResurrectionDetector");
            assertThat(context.getBeansOfType(ObservationDetector.class)).isNotEmpty();
            assertThat(context).hasSingleBean(SessionActionAdvisor.class);
            assertThat(context).hasSingleBean(SessionIntelligenceListener.class);
        });
    }
}
