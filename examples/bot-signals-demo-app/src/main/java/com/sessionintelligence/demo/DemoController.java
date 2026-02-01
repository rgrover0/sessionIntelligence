package com.sessionintelligence.demo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.sessionintelligence.windowsession.SessionKey;
import com.sessionintelligence.windowsession.WindowScopedAttributeAccessor;

@RestController
public class DemoController {
    private final WindowScopedAttributeAccessor accessor;
    private final SignalStore store;

    public DemoController(WindowScopedAttributeAccessor accessor, SignalStore store) {
        this.accessor = accessor;
        this.store = store;
    }

    @GetMapping("/start")
    public SessionInfo start(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        SessionKey key = accessor.getSessionKey(request);
        return new SessionInfo(
                session != null ? session.getId() : null,
                key != null ? key.windowName() : null
        );
    }

    @GetMapping("/whoami")
    public SessionInfo whoami(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        SessionKey key = accessor.getSessionKey(request);
        return new SessionInfo(
                session != null ? session.getId() : null,
                key != null ? key.windowName() : null
        );
    }

    @GetMapping("/rate")
    public String rate(@RequestHeader(name = "X-Window-Name", required = false) String windowName) {
        return windowName == null ? "window-missing" : "window=" + windowName;
    }

    @GetMapping("/signals/{sessionId}")
    public SignalSnapshot signals(@PathVariable("sessionId") String sessionId) {
        return store.snapshot(sessionId);
    }

    public record SessionInfo(String sessionId, String windowName) {
    }
}
