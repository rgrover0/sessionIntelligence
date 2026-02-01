package com.sessionintelligence.botsignals.starter;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sessionintelligence.botsignals.BotSignalsEngine;
import com.sessionintelligence.botsignals.RequestObservation;
import com.sessionintelligence.windowsession.SessionKey;
import com.sessionintelligence.windowsession.WindowScopedAttributeAccessor;
import com.sessionintelligence.windowsession.WindowSessionKeyResolver;

public class BotSignalsFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(BotSignalsFilter.class);

    private final BotSignalsEngine engine;
    private final BotSignalsProperties properties;
    private final BotSignalsHasher hasher;
    private final WindowSessionKeyResolver keyResolver;
    private final WindowScopedAttributeAccessor attributeAccessor;
    private final PathMatcher pathMatcher = new AntPathMatcher();

    public BotSignalsFilter(
            BotSignalsEngine engine,
            BotSignalsProperties properties,
            BotSignalsHasher hasher,
            WindowSessionKeyResolver keyResolver,
            WindowScopedAttributeAccessor attributeAccessor
    ) {
        this.engine = engine;
        this.properties = properties;
        this.hasher = hasher;
        this.keyResolver = keyResolver;
        this.attributeAccessor = attributeAccessor;
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
                log.warn("Bot signals observation failed", ex);
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
        SessionKey sessionKey = resolveSessionKey(request);
        PrincipalInfo principal = resolvePrincipalInfo();
        String rawUserAgent = request.getHeader("User-Agent");
        String userAgent = properties.getPrivacy().isStoreRawUserAgent() ? rawUserAgent : null;
        String userAgentHash = hasher != null ? hasher.hashValue(rawUserAgent) : null;
        String userAgentFamily = extractUaFamily(rawUserAgent);
        String userAgentMajor = extractUaMajor(rawUserAgent);
        String clientIp = null;
        String clientIpHash = null;
        if (properties.getPrivacy().isIpSignalsEnabled()) {
            String remoteAddr = request.getRemoteAddr();
            String ipSegment = truncateIp(remoteAddr);
            if (properties.getPrivacy().isStoreRawIp()) {
                clientIp = remoteAddr;
            }
            if (hasher != null) {
                clientIpHash = hasher.hashValue(ipSegment);
            }
        }

        return new RequestObservation(
                Instant.now(),
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                clientIp,
                clientIpHash,
                userAgent,
                userAgentHash,
                userAgentFamily,
                userAgentMajor,
                request.getHeader("Accept-Language"),
                request.getHeader("Accept-Encoding"),
                headerNames(request),
                principal.name(),
                principal.authenticated(),
                sessionKey
        );
    }

    private SessionKey resolveSessionKey(HttpServletRequest request) {
        if (attributeAccessor != null) {
            SessionKey key = attributeAccessor.getSessionKey(request);
            if (key != null) {
                return key;
            }
        }
        if (keyResolver != null) {
            return keyResolver.resolveSessionKey(request);
        }
        return null;
    }

    private Set<String> headerNames(HttpServletRequest request) {
        Enumeration<String> names = request.getHeaderNames();
        if (names == null) {
            return Set.of();
        }
        int maxHeaders = properties.getSafety().getMaxHeaderCount();
        int maxNameLength = properties.getSafety().getMaxHeaderNameLength();
        if (maxHeaders <= 0) {
            return Set.of();
        }
        Set<String> collected = new HashSet<>(Math.min(maxHeaders, 16));
        while (names.hasMoreElements() && collected.size() < maxHeaders) {
            String name = names.nextElement();
            if (name == null || name.isBlank()) {
                continue;
            }
            String normalized = name.trim().toLowerCase();
            if (normalized.length() > maxNameLength) {
                continue;
            }
            collected.add(normalized);
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
            log.debug("Bot signals principal lookup failed", ex);
            return PrincipalInfo.anonymous();
        }
    }

    private record PrincipalInfo(String name, boolean authenticated) {
        static PrincipalInfo anonymous() {
            return new PrincipalInfo(null, false);
        }
    }

    private String extractUaFamily(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "unknown";
        }
        String[] tokens = userAgent.trim().split("\\s+");
        String first = tokens[0];
        int slash = first.indexOf('/');
        if (slash > 0) {
            return first.substring(0, slash);
        }
        return first;
    }

    private String extractUaMajor(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "0";
        }
        String[] tokens = userAgent.trim().split("\\s+");
        String first = tokens[0];
        int slash = first.indexOf('/');
        if (slash < 0 || slash == first.length() - 1) {
            return "0";
        }
        String version = first.substring(slash + 1);
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < version.length(); i++) {
            char ch = version.charAt(i);
            if (Character.isDigit(ch)) {
                digits.append(ch);
            } else {
                break;
            }
        }
        return digits.length() == 0 ? "0" : digits.toString();
    }

    private String truncateIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return "";
        }
        if (ip.contains(".")) {
            String[] parts = ip.split("\\.");
            if (parts.length >= 3) {
                return parts[0] + "." + parts[1] + "." + parts[2];
            }
            return ip;
        }
        if (ip.contains(":")) {
            String[] parts = ip.split(":");
            StringBuilder truncated = new StringBuilder();
            int limit = Math.min(parts.length, 4);
            for (int i = 0; i < limit; i++) {
                if (i > 0) {
                    truncated.append(':');
                }
                truncated.append(parts[i]);
            }
            return truncated.toString();
        }
        return ip;
    }
}
