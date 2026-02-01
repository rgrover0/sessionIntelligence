package com.sessionintelligence.botsignals;

import java.util.List;

public record Fingerprint(String currentHash, List<String> previousHashes) {
    public Fingerprint {
        if (previousHashes == null) {
            previousHashes = List.of();
        } else {
            previousHashes = List.copyOf(previousHashes);
        }
    }
}
