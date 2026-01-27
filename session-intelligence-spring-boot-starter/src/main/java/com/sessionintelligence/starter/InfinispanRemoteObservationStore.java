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

public class InfinispanRemoteObservationStore implements ObservationStore {
    private static final Logger log = LoggerFactory.getLogger(InfinispanRemoteObservationStore.class);

    private final String telemetryCacheName;

    public InfinispanRemoteObservationStore(SessionIntelligenceProperties properties) {
        this.telemetryCacheName = properties.getStorage().getTelemetryCacheName();
        log.info("Session intelligence using Infinispan cache {}", telemetryCacheName);
    }

    @Override
    public SnapshotUpdate<SessionSnapshot> recordSession(
            RequestObservation observation,
            Fingerprint fingerprint
    ) {
        // TODO: Implement with Infinispan remote cache + proto schema.
        return null;
    }

    @Override
    public SnapshotUpdate<WindowSnapshot> recordWindow(
            RequestObservation observation,
            Fingerprint fingerprint
    ) {
        // TODO: Implement with Infinispan remote cache + proto schema.
        return null;
    }

    @Override
    public void save(SessionRiskScore score) {
        // TODO: Implement with Infinispan remote cache + proto schema.
    }
}
