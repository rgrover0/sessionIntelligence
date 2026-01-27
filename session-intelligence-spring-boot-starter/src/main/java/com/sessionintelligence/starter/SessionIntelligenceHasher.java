package com.sessionintelligence.starter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SessionIntelligenceHasher {
    private static final int SHORT_HASH_LENGTH = 12;

    private final String salt;

    public SessionIntelligenceHasher(SessionIntelligenceProperties properties) {
        String configured = properties.getFingerprint().getSalt();
        this.salt = configured == null ? "" : configured;
    }

    public String hashValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String payload = salt + "|" + value;
        String hashed = sha256(payload);
        if (hashed.length() <= SHORT_HASH_LENGTH) {
            return hashed;
        }
        return hashed.substring(0, SHORT_HASH_LENGTH);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
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
