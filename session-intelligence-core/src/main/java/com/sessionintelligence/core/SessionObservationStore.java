package com.sessionintelligence.core;

public interface SessionObservationStore {
    SnapshotUpdate<SessionSnapshot> recordSession(RequestObservation observation, Fingerprint fingerprint);
}
