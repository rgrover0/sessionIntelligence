package com.sessionintelligence.core;

public record DetectionContext(
        RequestObservation observation,
        SnapshotUpdate<SessionSnapshot> sessionUpdate,
        SnapshotUpdate<WindowSnapshot> windowUpdate,
        Fingerprint fingerprint
) {
}
