package com.sessionintelligence.core;

public enum ReasonCode {
    NONE,
    SESSION_REUSE,
    SESSION_RESURRECTION,
    WINDOW_COLLISION,
    WINDOW_EXPLOSION,
    FINGERPRINT_DRIFT,
    HIGH_REQUEST_RATE,
    MISSING_HEADERS,
    OTHER
}
