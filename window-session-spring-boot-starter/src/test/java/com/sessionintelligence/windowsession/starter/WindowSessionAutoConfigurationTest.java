package com.sessionintelligence.windowsession.starter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.sessionintelligence.windowsession.WindowNameResolver;
import com.sessionintelligence.windowsession.WindowScopedAttributeAccessor;
import com.sessionintelligence.windowsession.WindowSessionKeyResolver;

import static org.assertj.core.api.Assertions.assertThat;

class WindowSessionAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(WindowSessionAutoConfiguration.class))
            .withPropertyValues("window-session.enabled=true");

    @Test
    void contextLoads() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(WindowSessionFilter.class);
            assertThat(context).hasSingleBean(WindowNameResolver.class);
            assertThat(context).hasSingleBean(WindowSessionKeyResolver.class);
            assertThat(context).hasSingleBean(WindowScopedAttributeAccessor.class);
        });
    }
}
