package com.sessionintelligence.core;

public interface SessionObservationStore {
    SessionSnapshot record(RequestObservation observation, Fingerprint fingerprint);
}
