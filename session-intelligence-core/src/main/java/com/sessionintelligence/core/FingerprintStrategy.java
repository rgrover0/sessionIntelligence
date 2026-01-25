package com.sessionintelligence.core;

public interface FingerprintStrategy {
    Fingerprint fingerprint(RequestObservation observation);
}
