package com.sessionintelligence.core;

public interface WindowObservationStore {
    SnapshotUpdate<WindowSnapshot> recordWindow(RequestObservation observation, Fingerprint fingerprint);
}
