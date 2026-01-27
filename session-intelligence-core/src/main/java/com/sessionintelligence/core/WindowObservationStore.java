package com.sessionintelligence.core;

public interface WindowObservationStore {
    SnapshotUpdate<WindowSnapshot> record(RequestObservation observation, Fingerprint fingerprint);
}
