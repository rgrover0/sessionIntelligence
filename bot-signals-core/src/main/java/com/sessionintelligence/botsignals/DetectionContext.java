package com.sessionintelligence.botsignals;

public record DetectionContext(
        RequestObservation observation,
        SnapshotUpdate<SessionSnapshot> sessionUpdate,
        SnapshotUpdate<WindowSnapshot> windowUpdate,
        Fingerprint fingerprint
) {
}
