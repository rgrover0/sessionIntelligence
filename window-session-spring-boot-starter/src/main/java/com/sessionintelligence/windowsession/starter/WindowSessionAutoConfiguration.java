package com.sessionintelligence.windowsession.starter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.sessionintelligence.windowsession.WindowNameResolver;
import com.sessionintelligence.windowsession.WindowScopedAttributeAccessor;
import com.sessionintelligence.windowsession.WindowSessionKeyResolver;

@AutoConfiguration
@EnableConfigurationProperties(WindowSessionProperties.class)
@ConditionalOnProperty(
        prefix = "window-session",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class WindowSessionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public WindowNameResolver windowNameResolver(WindowSessionProperties properties) {
        return new DefaultWindowNameResolver(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public WindowSessionKeyResolver windowSessionKeyResolver(WindowNameResolver windowNameResolver) {
        return new DefaultWindowSessionKeyResolver(windowNameResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public WindowScopedAttributeAccessor windowScopedAttributeAccessor() {
        return new RequestAttributeWindowScopedAttributeAccessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public WindowSessionFilter windowSessionFilter(
            WindowSessionKeyResolver resolver,
            WindowScopedAttributeAccessor accessor
    ) {
        return new WindowSessionFilter(resolver, accessor);
    }
}
