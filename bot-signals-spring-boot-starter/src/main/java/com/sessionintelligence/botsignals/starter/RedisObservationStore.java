package com.sessionintelligence.botsignals.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sessionintelligence.botsignals.Fingerprint;
import com.sessionintelligence.botsignals.ObservationStore;
import com.sessionintelligence.botsignals.RequestObservation;
import com.sessionintelligence.botsignals.SessionRiskScore;
import com.sessionintelligence.botsignals.SessionSnapshot;
import com.sessionintelligence.botsignals.SnapshotUpdate;
import com.sessionintelligence.botsignals.WindowSnapshot;

public class RedisObservationStore implements ObservationStore {
    private static final Logger log = LoggerFactory.getLogger(RedisObservationStore.class);

    public RedisObservationStore() {
        log.info("Bot signals Redis store is not yet implemented");
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
