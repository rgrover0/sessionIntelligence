package com.sessionintelligence.core;

public interface WindowObservationStore {
    void record(RequestObservation observation, Fingerprint fingerprint);
}
