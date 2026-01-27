package com.sessionintelligence.otel;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import com.sessionintelligence.core.SessionIntelligenceListener;
import com.sessionintelligence.starter.SessionIntelligenceHasher;

@AutoConfiguration
@ConditionalOnClass(name = "io.opentelemetry.api.OpenTelemetry")
@ConditionalOnProperty(
        prefix = "session-intelligence.telemetry",
        name = "otel-enabled",
        havingValue = "true"
)
public class SessionIntelligenceOtelAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "otelSessionIntelligenceListener")
    public SessionIntelligenceListener otelSessionIntelligenceListener(SessionIntelligenceHasher hasher) {
        return new OtelSessionIntelligenceListener(hasher);
    }
}
