package com.sessionintelligence.windowsession;

import jakarta.servlet.http.HttpServletRequest;

public interface WindowScopedAttributeAccessor {
    String SESSION_KEY_ATTRIBUTE = WindowScopedAttributeAccessor.class.getName() + ".SESSION_KEY";

    void setSessionKey(HttpServletRequest request, SessionKey sessionKey);

    SessionKey getSessionKey(HttpServletRequest request);
}
