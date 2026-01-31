package com.sessionintelligence.botsignals;

public interface WindowObservationStore {
    SnapshotUpdate<WindowSnapshot> recordWindow(RequestObservation observation, Fingerprint fingerprint);
}
