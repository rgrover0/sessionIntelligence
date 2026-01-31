package com.sessionintelligence.windowsession;

import jakarta.servlet.http.HttpServletRequest;

public interface WindowSessionKeyResolver {
    SessionKey resolveSessionKey(HttpServletRequest request);
}
