package com.sessionintelligence.core;

public interface SessionObservationStore {
    SnapshotUpdate<SessionSnapshot> record(RequestObservation observation, Fingerprint fingerprint);
}
