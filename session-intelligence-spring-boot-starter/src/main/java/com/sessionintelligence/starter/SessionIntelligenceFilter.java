package com.sessionintelligence.starter;

import java.io.IOException;
import java.time.Instant;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sessionintelligence.core.RequestObservation;
import com.sessionintelligence.core.SessionIntelligenceEngine;
import com.sessionintelligence.core.SessionKey;

public class SessionIntelligenceFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(SessionIntelligenceFilter.class);

    private final SessionIntelligenceEngine engine;
    private final SessionIntelligenceProperties properties;
    private final PathMatcher pathMatcher = new AntPathMatcher();

    public SessionIntelligenceFilter(
            SessionIntelligenceEngine engine,
            SessionIntelligenceProperties properties
    ) {
        this.engine = engine;
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (matchesAny(properties.getExcludePathPatterns(), path)) {
            return true;
        }
        List<String> includes = properties.getIncludePathPatterns();
        if (includes == null || includes.isEmpty()) {
            return false;
        }
        return !matchesAny(includes, path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        boolean shouldSample = shouldSample();
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (!shouldSample) {
                return;
            }
            try {
                RequestObservation observation = buildObservation(request, response);
                engine.observe(observation);
            } catch (Exception ex) {
                log.warn("Session intelligence observation failed", ex);
            }
        }
    }

    private boolean shouldSample() {
        double rate = properties.getSampleRate();
        if (rate >= 1.0d) {
            return true;
        }
        if (rate <= 0.0d) {
            return false;
        }
        return ThreadLocalRandom.current().nextDouble() < rate;
    }

    private boolean matchesAny(List<String> patterns, String path) {
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }
        for (String pattern : patterns) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private RequestObservation buildObservation(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String windowName = request.getHeader(properties.getWindowHeaderName());
        HttpSession session = request.getSession(false);
        String sessionId = session != null ? session.getId() : null;
        SessionKey sessionKey = new SessionKey(sessionId, windowName);
        PrincipalInfo principal = resolvePrincipalInfo();

        return new RequestObservation(
                Instant.now(),
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                request.getHeader("Accept-Language"),
                request.getHeader("Accept-Encoding"),
                headerNames(request),
                principal.name(),
                principal.authenticated(),
                sessionKey
        );
    }

    private Set<String> headerNames(HttpServletRequest request) {
        Enumeration<String> names = request.getHeaderNames();
        if (names == null) {
            return Set.of();
        }
        Set<String> collected = new HashSet<>();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (name != null && !name.isBlank()) {
                collected.add(name);
            }
        }
        return collected;
    }

    private PrincipalInfo resolvePrincipalInfo() {
        try {
            Class<?> holder = Class.forName("org.springframework.security.core.context.SecurityContextHolder");
            Object context = holder.getMethod("getContext").invoke(null);
            if (context == null) {
                return PrincipalInfo.anonymous();
            }
            Object authentication = context.getClass().getMethod("getAuthentication").invoke(context);
            if (authentication == null) {
                return PrincipalInfo.anonymous();
            }
            boolean authenticated = Boolean.TRUE.equals(
                    authentication.getClass().getMethod("isAuthenticated").invoke(authentication)
            );
            Object name = authentication.getClass().getMethod("getName").invoke(authentication);
            return new PrincipalInfo(name != null ? name.toString() : null, authenticated);
        } catch (ClassNotFoundException ex) {
            return PrincipalInfo.anonymous();
        } catch (Exception ex) {
            log.debug("Session intelligence principal lookup failed", ex);
            return PrincipalInfo.anonymous();
        }
    }

    private record PrincipalInfo(String name, boolean authenticated) {
        static PrincipalInfo anonymous() {
            return new PrincipalInfo(null, false);
        }
    }
}
