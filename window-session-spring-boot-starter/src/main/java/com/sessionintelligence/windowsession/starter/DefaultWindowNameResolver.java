package com.sessionintelligence.windowsession.starter;

import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;

import com.sessionintelligence.windowsession.WindowNameResolver;

public class DefaultWindowNameResolver implements WindowNameResolver {
    private final WindowSessionProperties properties;
    private final Pattern windowNamePattern;

    public DefaultWindowNameResolver(WindowSessionProperties properties) {
        this.properties = properties;
        this.windowNamePattern = compilePattern(properties.getWindowNamePattern());
    }

    @Override
    public String resolveWindowName(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String headerValue = request.getHeader(properties.getWindowHeaderName());
        if (headerValue == null) {
            return null;
        }
        String trimmed = headerValue.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        int maxLength = properties.getMaxWindowNameLength();
        if (maxLength > 0 && trimmed.length() > maxLength) {
            return null;
        }
        if (windowNamePattern != null && !windowNamePattern.matcher(trimmed).matches()) {
            return null;
        }
        return trimmed;
    }

    private Pattern compilePattern(String pattern) {
        if (pattern == null || pattern.isBlank()) {
            return null;
        }
        return Pattern.compile(pattern);
    }
}
