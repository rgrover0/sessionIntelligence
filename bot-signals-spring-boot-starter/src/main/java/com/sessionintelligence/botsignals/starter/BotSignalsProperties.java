package com.sessionintelligence.botsignals.starter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.sessionintelligence.botsignals.ReasonCode;
import com.sessionintelligence.botsignals.Severity;

@ConfigurationProperties(prefix = "bot-signals")
public class BotSignalsProperties {
    private boolean enabled = true;
    private Features features = new Features();
    private List<String> includePathPatterns = new ArrayList<>();
    private List<String> excludePathPatterns = new ArrayList<>();
    private double sampleRate = 1.0d;
    private Thresholds thresholds = new Thresholds();
    private Storage storage = new Storage();
    private Telemetry telemetry = new Telemetry();
    private Privacy privacy = new Privacy();
    private Safety safety = new Safety();
    private Scoring scoring = new Scoring();
    private FingerprintProperties fingerprint = new FingerprintProperties();

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

    public Privacy getPrivacy() {
        return privacy;
    }

    public void setPrivacy(Privacy privacy) {
        this.privacy = privacy;
    }

    public Safety getSafety() {
        return safety;
    }

    public void setSafety(Safety safety) {
        this.safety = safety;
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

    public static class Features {
        private boolean riskScoring = true;
        private boolean anomalyDetection = true;

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

        public Backend getBackend() {
            return backend;
        }

        public void setBackend(Backend backend) {
            this.backend = backend;
        }

        public enum Backend {
            IN_MEMORY,
            REDIS
        }
    }

    public static class Telemetry {
        private boolean metricsEnabled = true;

        public boolean isMetricsEnabled() {
            return metricsEnabled;
        }

        public void setMetricsEnabled(boolean metricsEnabled) {
            this.metricsEnabled = metricsEnabled;
        }
    }

    public static class Privacy {
        private boolean storeRawIp = false;
        private boolean storeRawUserAgent = false;
        private boolean ipSignalsEnabled = false;
        private Duration retentionTtl = Duration.ofHours(24);

        public boolean isStoreRawIp() {
            return storeRawIp;
        }

        public void setStoreRawIp(boolean storeRawIp) {
            this.storeRawIp = storeRawIp;
        }

        public boolean isStoreRawUserAgent() {
            return storeRawUserAgent;
        }

        public void setStoreRawUserAgent(boolean storeRawUserAgent) {
            this.storeRawUserAgent = storeRawUserAgent;
        }

        public boolean isIpSignalsEnabled() {
            return ipSignalsEnabled;
        }

        public void setIpSignalsEnabled(boolean ipSignalsEnabled) {
            this.ipSignalsEnabled = ipSignalsEnabled;
        }

        public Duration getRetentionTtl() {
            return retentionTtl;
        }

        public void setRetentionTtl(Duration retentionTtl) {
            this.retentionTtl = retentionTtl;
        }
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

    public static class Safety {
        private boolean failOpenEnabled = true;
        private int circuitBreakerFailureThreshold = 3;
        private Duration circuitBreakerOpenDuration = Duration.ofSeconds(30);
        private int maxHeaderCount = 50;
        private int maxHeaderNameLength = 64;
        private int maxWindowNamesTracked = 100;

        public boolean isFailOpenEnabled() {
            return failOpenEnabled;
        }

        public void setFailOpenEnabled(boolean failOpenEnabled) {
            this.failOpenEnabled = failOpenEnabled;
        }

        public int getCircuitBreakerFailureThreshold() {
            return circuitBreakerFailureThreshold;
        }

        public void setCircuitBreakerFailureThreshold(int circuitBreakerFailureThreshold) {
            this.circuitBreakerFailureThreshold = circuitBreakerFailureThreshold;
        }

        public Duration getCircuitBreakerOpenDuration() {
            return circuitBreakerOpenDuration;
        }

        public void setCircuitBreakerOpenDuration(Duration circuitBreakerOpenDuration) {
            this.circuitBreakerOpenDuration = circuitBreakerOpenDuration;
        }

        public int getMaxHeaderCount() {
            return maxHeaderCount;
        }

        public void setMaxHeaderCount(int maxHeaderCount) {
            this.maxHeaderCount = maxHeaderCount;
        }

        public int getMaxHeaderNameLength() {
            return maxHeaderNameLength;
        }

        public void setMaxHeaderNameLength(int maxHeaderNameLength) {
            this.maxHeaderNameLength = maxHeaderNameLength;
        }

        public int getMaxWindowNamesTracked() {
            return maxWindowNamesTracked;
        }

        public void setMaxWindowNamesTracked(int maxWindowNamesTracked) {
            this.maxWindowNamesTracked = maxWindowNamesTracked;
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
