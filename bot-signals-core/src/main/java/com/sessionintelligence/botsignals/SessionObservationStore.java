package com.sessionintelligence.botsignals;

public interface SessionObservationStore {
    SnapshotUpdate<SessionSnapshot> recordSession(RequestObservation observation, Fingerprint fingerprint);
}
