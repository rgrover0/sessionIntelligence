package com.sessionintelligence.starter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.sessionintelligence.core.ReasonCode;
import com.sessionintelligence.core.Severity;

@ConfigurationProperties(prefix = "session-intelligence")
public class SessionIntelligenceProperties {
    private boolean enabled = true;
    private Features features = new Features();
    private Headers headers = new Headers();
    private List<String> includePathPatterns = new ArrayList<>();
    private List<String> excludePathPatterns = new ArrayList<>();
    private double sampleRate = 1.0d;
    private Thresholds thresholds = new Thresholds();
    private Storage storage = new Storage();
    private Telemetry telemetry = new Telemetry();
    private FingerprintProperties fingerprint = new FingerprintProperties();
    private Scoring scoring = new Scoring();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Features getFeatures() {
        return features;
    }

    public void setFeatures(Features features) {
        this.features = features;
    }

    public Headers getHeaders() {
        return headers;
    }

    public void setHeaders(Headers headers) {
        this.headers = headers;
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

    public Thresholds getThresholds() {
        return thresholds;
    }

    public void setThresholds(Thresholds thresholds) {
        this.thresholds = thresholds;
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public Telemetry getTelemetry() {
        return telemetry;
    }

    public void setTelemetry(Telemetry telemetry) {
        this.telemetry = telemetry;
    }

    public Scoring getScoring() {
        return scoring;
    }

    public void setScoring(Scoring scoring) {
        this.scoring = scoring;
    }

    public FingerprintProperties getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(FingerprintProperties fingerprint) {
        this.fingerprint = fingerprint;
    }

    public static class FingerprintProperties {
        private String salt = "change-me";
        private List<String> additionalSalts = new ArrayList<>();
        private boolean includeIpSegment = false;

        public String getSalt() {
            return salt;
        }

        public void setSalt(String salt) {
            this.salt = salt;
        }

        public List<String> getAdditionalSalts() {
            return additionalSalts;
        }

        public void setAdditionalSalts(List<String> additionalSalts) {
            this.additionalSalts = additionalSalts;
        }

        public boolean isIncludeIpSegment() {
            return includeIpSegment;
        }

        public void setIncludeIpSegment(boolean includeIpSegment) {
            this.includeIpSegment = includeIpSegment;
        }
    }

    public static class Features {
        private boolean riskScoring = true;
        private boolean anomalyDetection = true;
        private boolean actionAdvisory = true;

        public boolean isRiskScoring() {
            return riskScoring;
        }

        public void setRiskScoring(boolean riskScoring) {
            this.riskScoring = riskScoring;
        }

        public boolean isAnomalyDetection() {
            return anomalyDetection;
        }

        public void setAnomalyDetection(boolean anomalyDetection) {
            this.anomalyDetection = anomalyDetection;
        }

        public boolean isActionAdvisory() {
            return actionAdvisory;
        }

        public void setActionAdvisory(boolean actionAdvisory) {
            this.actionAdvisory = actionAdvisory;
        }
    }

    public static class Headers {
        private String windowName = "X-Window-Name";

        public String getWindowName() {
            return windowName;
        }

        public void setWindowName(String windowName) {
            this.windowName = windowName;
        }
    }

    public static class Thresholds {
        private int maxRequestsPerMinute = 120;
        private int maxParallelWindows = 10;
        private Duration fingerprintDriftWindow = Duration.ofMinutes(5);
        private double fingerprintDriftTolerance = 0.5d;
        private Duration windowExplosionWindow = Duration.ofMinutes(2);
        private List<String> rateEndpointPatterns = new ArrayList<>();

        public int getMaxRequestsPerMinute() {
            return maxRequestsPerMinute;
        }

        public void setMaxRequestsPerMinute(int maxRequestsPerMinute) {
            this.maxRequestsPerMinute = maxRequestsPerMinute;
        }

        public int getMaxParallelWindows() {
            return maxParallelWindows;
        }

        public void setMaxParallelWindows(int maxParallelWindows) {
            this.maxParallelWindows = maxParallelWindows;
        }

        public Duration getFingerprintDriftWindow() {
            return fingerprintDriftWindow;
        }

        public void setFingerprintDriftWindow(Duration fingerprintDriftWindow) {
            this.fingerprintDriftWindow = fingerprintDriftWindow;
        }

        public double getFingerprintDriftTolerance() {
            return fingerprintDriftTolerance;
        }

        public void setFingerprintDriftTolerance(double fingerprintDriftTolerance) {
            this.fingerprintDriftTolerance = fingerprintDriftTolerance;
        }

        public Duration getWindowExplosionWindow() {
            return windowExplosionWindow;
        }

        public void setWindowExplosionWindow(Duration windowExplosionWindow) {
            this.windowExplosionWindow = windowExplosionWindow;
        }

        public List<String> getRateEndpointPatterns() {
            return rateEndpointPatterns;
        }

        public void setRateEndpointPatterns(List<String> rateEndpointPatterns) {
            this.rateEndpointPatterns = rateEndpointPatterns;
        }
    }

    public static class Storage {
        private Backend backend = Backend.IN_MEMORY;
        private String telemetryCacheName = "session-intelligence-telemetry";

        public Backend getBackend() {
            return backend;
        }

        public void setBackend(Backend backend) {
            this.backend = backend;
        }

        public String getTelemetryCacheName() {
            return telemetryCacheName;
        }

        public void setTelemetryCacheName(String telemetryCacheName) {
            this.telemetryCacheName = telemetryCacheName;
        }

        public enum Backend {
            IN_MEMORY,
            INFINISPAN_REMOTE,
            REDIS
        }
    }

    public static class Telemetry {
        private boolean publishApplicationEvents = true;
        private boolean otelEnabled = false;

        public boolean isPublishApplicationEvents() {
            return publishApplicationEvents;
        }

        public void setPublishApplicationEvents(boolean publishApplicationEvents) {
            this.publishApplicationEvents = publishApplicationEvents;
        }

        public boolean isOtelEnabled() {
            return otelEnabled;
        }

        public void setOtelEnabled(boolean otelEnabled) {
            this.otelEnabled = otelEnabled;
        }
    }

    public static class Scoring {
        private int maxScore = 100;
        private int defaultWeight = 5;
        private Duration decayWindow = Duration.ofMinutes(30);
        private Map<ReasonCode, Integer> weights = defaultWeights();
        private Map<Severity, Double> severityMultipliers = defaultSeverityMultipliers();

        public int getMaxScore() {
            return maxScore;
        }

        public void setMaxScore(int maxScore) {
            this.maxScore = maxScore;
        }

        public int getDefaultWeight() {
            return defaultWeight;
        }

        public void setDefaultWeight(int defaultWeight) {
            this.defaultWeight = defaultWeight;
        }

        public Duration getDecayWindow() {
            return decayWindow;
        }

        public void setDecayWindow(Duration decayWindow) {
            this.decayWindow = decayWindow;
        }

        public Map<ReasonCode, Integer> getWeights() {
            return weights;
        }

        public void setWeights(Map<ReasonCode, Integer> weights) {
            this.weights = weights;
        }

        public Map<Severity, Double> getSeverityMultipliers() {
            return severityMultipliers;
        }

        public void setSeverityMultipliers(Map<Severity, Double> severityMultipliers) {
            this.severityMultipliers = severityMultipliers;
        }

        private static Map<ReasonCode, Integer> defaultWeights() {
            Map<ReasonCode, Integer> values = new EnumMap<>(ReasonCode.class);
            values.put(ReasonCode.SESSION_REUSE, 15);
            values.put(ReasonCode.SESSION_RESURRECTION, 20);
            values.put(ReasonCode.WINDOW_COLLISION, 20);
            values.put(ReasonCode.WINDOW_EXPLOSION, 15);
            values.put(ReasonCode.FINGERPRINT_DRIFT, 10);
            values.put(ReasonCode.HIGH_REQUEST_RATE, 25);
            values.put(ReasonCode.MISSING_HEADERS, 10);
            values.put(ReasonCode.OTHER, 5);
            values.put(ReasonCode.NONE, 0);
            return values;
        }

        private static Map<Severity, Double> defaultSeverityMultipliers() {
            Map<Severity, Double> values = new EnumMap<>(Severity.class);
            values.put(Severity.LOW, 0.5d);
            values.put(Severity.MEDIUM, 1.0d);
            values.put(Severity.HIGH, 1.5d);
            values.put(Severity.CRITICAL, 2.0d);
            return values;
        }
    }
}
