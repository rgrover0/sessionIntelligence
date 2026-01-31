package com.sessionintelligence.botsignals;

public interface FingerprintStrategy {
    Fingerprint fingerprint(RequestObservation observation);
}
