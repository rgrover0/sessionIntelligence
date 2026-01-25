package com.sessionintelligence.core;

import java.util.List;

public class NoOpFingerprintStrategy implements FingerprintStrategy {
    @Override
    public Fingerprint fingerprint(RequestObservation observation) {
        return new Fingerprint("unknown", List.of());
    }
}
