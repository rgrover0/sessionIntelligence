package com.sessionintelligence.starter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.sessionintelligence.core.Fingerprint;
import com.sessionintelligence.core.FingerprintStrategy;
import com.sessionintelligence.core.RequestObservation;

public class DefaultFingerprintStrategy implements FingerprintStrategy {
    private final SessionIntelligenceProperties properties;

    public DefaultFingerprintStrategy(SessionIntelligenceProperties properties) {
        this.properties = properties;
    }

    @Override
    public Fingerprint fingerprint(RequestObservation observation) {
        String input = buildFingerprintInput(observation);
        List<String> additionalSalts = properties.getFingerprint().getAdditionalSalts();
        List<String> previousHashes = new ArrayList<>();
        if (additionalSalts != null) {
            for (String salt : additionalSalts) {
                if (salt != null && !salt.isBlank()) {
                    previousHashes.add(hashWithSalt(salt, input));
                }
            }
        }
        String currentHash = hashWithSalt(properties.getFingerprint().getSalt(), input);
        return new Fingerprint(currentHash, previousHashes);
    }

    private String buildFingerprintInput(RequestObservation observation) {
        String ua = observation != null ? observation.userAgent() : null;
        String uaFamily = extractUaFamily(ua);
        String uaMajor = extractUaMajor(ua);
        String acceptLanguage = observation != null ? nullSafe(observation.acceptLanguage()) : "";
        String acceptEncoding = observation != null ? nullSafe(observation.acceptEncoding()) : "";
        String headerNames = headerNamesValue(observation != null ? observation.headerNames() : null);
        String ipSegment = "";
        if (observation != null && properties.getFingerprint().isIncludeIpSegment()) {
            ipSegment = truncateIp(observation.clientIp());
        }
        return String.join("|", uaFamily, uaMajor, acceptLanguage, acceptEncoding, headerNames, ipSegment);
    }

    private String headerNamesValue(Set<String> headerNames) {
        if (headerNames == null || headerNames.isEmpty()) {
            return "";
        }
        List<String> sorted = new ArrayList<>(headerNames.size());
        for (String name : headerNames) {
            if (name != null) {
                sorted.add(name.toLowerCase(Locale.ROOT));
            }
        }
        Collections.sort(sorted);
        return String.join(",", sorted);
    }

    private String extractUaFamily(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "unknown";
        }
        String[] tokens = userAgent.trim().split("\\s+");
        String first = tokens[0];
        int slash = first.indexOf('/');
        if (slash > 0) {
            return first.substring(0, slash);
        }
        return first;
    }

    private String extractUaMajor(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "0";
        }
        String[] tokens = userAgent.trim().split("\\s+");
        String first = tokens[0];
        int slash = first.indexOf('/');
        if (slash < 0 || slash == first.length() - 1) {
            return "0";
        }
        String version = first.substring(slash + 1);
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < version.length(); i++) {
            char ch = version.charAt(i);
            if (Character.isDigit(ch)) {
                digits.append(ch);
            } else {
                break;
            }
        }
        return digits.length() == 0 ? "0" : digits.toString();
    }

    private String truncateIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return "";
        }
        if (ip.contains(".")) {
            String[] parts = ip.split("\\.");
            if (parts.length >= 3) {
                return parts[0] + "." + parts[1] + "." + parts[2];
            }
            return ip;
        }
        if (ip.contains(":")) {
            String[] parts = ip.split(":");
            StringBuilder truncated = new StringBuilder();
            int limit = Math.min(parts.length, 4);
            for (int i = 0; i < limit; i++) {
                if (i > 0) {
                    truncated.append(':');
                }
                truncated.append(parts[i]);
            }
            return truncated.toString();
        }
        return ip;
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private String hashWithSalt(String salt, String input) {
        String effectiveSalt = salt == null ? "" : salt;
        String payload = effectiveSalt + "|" + input;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return toHex(bytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            String hex = Integer.toHexString(value & 0xFF);
            if (hex.length() == 1) {
                builder.append('0');
            }
            builder.append(hex);
        }
        return builder.toString();
    }
}
