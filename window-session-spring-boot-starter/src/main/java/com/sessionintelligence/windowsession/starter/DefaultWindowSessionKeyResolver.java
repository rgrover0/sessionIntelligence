package com.sessionintelligence.windowsession.starter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import com.sessionintelligence.windowsession.SessionKey;
import com.sessionintelligence.windowsession.WindowNameResolver;
import com.sessionintelligence.windowsession.WindowSessionKeyResolver;

public class DefaultWindowSessionKeyResolver implements WindowSessionKeyResolver {
    private final WindowNameResolver windowNameResolver;

    public DefaultWindowSessionKeyResolver(WindowNameResolver windowNameResolver) {
        this.windowNameResolver = windowNameResolver;
    }

    @Override
    public SessionKey resolveSessionKey(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        HttpSession session = request.getSession(false);
        String sessionId = session != null ? session.getId() : null;
        String windowName = windowNameResolver != null
                ? windowNameResolver.resolveWindowName(request)
                : null;
        if ((sessionId == null || sessionId.isBlank())
                && (windowName == null || windowName.isBlank())) {
            return null;
        }
        return new SessionKey(sessionId, windowName);
    }
}
