package com.sessionintelligence.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sessionintelligence.core.Fingerprint;
import com.sessionintelligence.core.ObservationStore;
import com.sessionintelligence.core.RequestObservation;
import com.sessionintelligence.core.SessionRiskScore;
import com.sessionintelligence.core.SessionSnapshot;
import com.sessionintelligence.core.SnapshotUpdate;
import com.sessionintelligence.core.WindowSnapshot;

public class RedisObservationStore implements ObservationStore {
    private static final Logger log = LoggerFactory.getLogger(RedisObservationStore.class);

    public RedisObservationStore() {
        log.info("Session intelligence Redis store is not yet implemented");
    }

    @Override
    public SnapshotUpdate<SessionSnapshot> recordSession(
            RequestObservation observation,
            Fingerprint fingerprint
    ) {
        return null;
    }

    @Override
    public SnapshotUpdate<WindowSnapshot> recordWindow(
            RequestObservation observation,
            Fingerprint fingerprint
    ) {
        return null;
    }

    @Override
    public void save(SessionRiskScore score) {
        // no-op placeholder
    }
}
