package com.sessionintelligence.core;

public enum ReasonCode {
    NONE,
    SESSION_REUSE,
    WINDOW_COLLISION,
    FINGERPRINT_DRIFT,
    HIGH_REQUEST_RATE,
    MISSING_HEADERS,
    OTHER
}
