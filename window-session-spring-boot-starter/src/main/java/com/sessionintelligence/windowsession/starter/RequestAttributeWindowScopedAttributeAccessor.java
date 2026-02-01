package com.sessionintelligence.windowsession.starter;

import jakarta.servlet.http.HttpServletRequest;

import com.sessionintelligence.windowsession.SessionKey;
import com.sessionintelligence.windowsession.WindowScopedAttributeAccessor;

public class RequestAttributeWindowScopedAttributeAccessor implements WindowScopedAttributeAccessor {
    @Override
    public void setSessionKey(HttpServletRequest request, SessionKey sessionKey) {
        if (request == null) {
            return;
        }
        request.setAttribute(SESSION_KEY_ATTRIBUTE, sessionKey);
    }

    @Override
    public SessionKey getSessionKey(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object value = request.getAttribute(SESSION_KEY_ATTRIBUTE);
        if (value instanceof SessionKey) {
            return (SessionKey) value;
        }
        return null;
    }
}
