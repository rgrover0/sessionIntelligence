package com.sessionintelligence.windowsession.starter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sessionintelligence.windowsession.SessionKey;
import com.sessionintelligence.windowsession.WindowScopedAttributeAccessor;
import com.sessionintelligence.windowsession.WindowSessionKeyResolver;

public class WindowSessionFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(WindowSessionFilter.class);

    private final WindowSessionKeyResolver keyResolver;
    private final WindowScopedAttributeAccessor attributeAccessor;

    public WindowSessionFilter(
            WindowSessionKeyResolver keyResolver,
            WindowScopedAttributeAccessor attributeAccessor
    ) {
        this.keyResolver = keyResolver;
        this.attributeAccessor = attributeAccessor;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            SessionKey key = keyResolver != null ? keyResolver.resolveSessionKey(request) : null;
            if (key != null && attributeAccessor != null) {
                attributeAccessor.setSessionKey(request, key);
            }
        } catch (Exception ex) {
            log.debug("Window session keying failed", ex);
        } finally {
            filterChain.doFilter(request, response);
        }
    }
}
