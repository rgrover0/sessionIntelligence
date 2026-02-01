package com.sessionintelligence.windowsession.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "window-session")
public class WindowSessionProperties {
    private boolean enabled = true;
    private String windowHeaderName = "X-Window-Name";
    private int maxWindowNameLength = 64;
    private String windowNamePattern = "^[A-Za-z0-9._-]{1,64}$";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getWindowHeaderName() {
        return windowHeaderName;
    }

    public void setWindowHeaderName(String windowHeaderName) {
        this.windowHeaderName = windowHeaderName;
    }

    public int getMaxWindowNameLength() {
        return maxWindowNameLength;
    }

    public void setMaxWindowNameLength(int maxWindowNameLength) {
        this.maxWindowNameLength = maxWindowNameLength;
    }

    public String getWindowNamePattern() {
        return windowNamePattern;
    }

    public void setWindowNamePattern(String windowNamePattern) {
        this.windowNamePattern = windowNamePattern;
    }
}
