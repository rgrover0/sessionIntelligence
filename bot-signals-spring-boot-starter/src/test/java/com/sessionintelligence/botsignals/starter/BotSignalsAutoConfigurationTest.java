package com.sessionintelligence.botsignals.starter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.sessionintelligence.botsignals.BotSignalsEngine;
import com.sessionintelligence.botsignals.BotSignalsListener;
import com.sessionintelligence.botsignals.FingerprintStrategy;
import com.sessionintelligence.botsignals.ObservationDetector;
import com.sessionintelligence.botsignals.ObservationStore;
import com.sessionintelligence.botsignals.RiskScorer;

import static org.assertj.core.api.Assertions.assertThat;

class BotSignalsAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(BotSignalsAutoConfiguration.class))
            .withPropertyValues("bot-signals.enabled=true");

    @Test
    void contextLoads() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(BotSignalsEngine.class);
            assertThat(context).hasSingleBean(BotSignalsFilter.class);
            assertThat(context).hasBean("botSignalsObservationStore");
            assertThat(context.getBean(ObservationStore.class)).isInstanceOf(FailOpenObservationStore.class);
            assertThat(context).hasSingleBean(FingerprintStrategy.class);
            assertThat(context).hasSingleBean(RiskScorer.class);
            assertThat(context.getBeansOfType(ObservationDetector.class)).isNotEmpty();
            assertThat(context).hasSingleBean(BotSignalsListener.class);
        });
    }
}
