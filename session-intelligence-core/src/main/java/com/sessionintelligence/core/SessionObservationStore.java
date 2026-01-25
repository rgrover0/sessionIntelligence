package com.sessionintelligence.core;

public interface SessionObservationStore {
    void record(RequestObservation observation, Fingerprint fingerprint);
}
