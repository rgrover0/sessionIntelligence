package com.sessionintelligence.starter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "session-intelligence")
public class SessionIntelligenceProperties {
    private boolean enabled = true;
    private String windowHeaderName = "X-Window-Name";
    private List<String> includePathPatterns = new ArrayList<>();
    private List<String> excludePathPatterns = new ArrayList<>();
    private double sampleRate = 1.0d;

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

    public List<String> getIncludePathPatterns() {
        return includePathPatterns;
    }

    public void setIncludePathPatterns(List<String> includePathPatterns) {
        this.includePathPatterns = includePathPatterns;
    }

    public List<String> getExcludePathPatterns() {
        return excludePathPatterns;
    }

    public void setExcludePathPatterns(List<String> excludePathPatterns) {
        this.excludePathPatterns = excludePathPatterns;
    }

    public double getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(double sampleRate) {
        this.sampleRate = sampleRate;
    }
}
