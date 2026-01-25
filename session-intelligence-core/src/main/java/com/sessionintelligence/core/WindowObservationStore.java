package com.sessionintelligence.core;

public interface WindowObservationStore {
    WindowSnapshot record(RequestObservation observation, Fingerprint fingerprint);
}
