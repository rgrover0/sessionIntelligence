package com.sessionintelligence.windowsession;

import jakarta.servlet.http.HttpServletRequest;

public interface WindowNameResolver {
    String resolveWindowName(HttpServletRequest request);
}
