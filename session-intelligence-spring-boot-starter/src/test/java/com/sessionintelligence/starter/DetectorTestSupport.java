package com.sessionintelligence.starter;

import java.time.Instant;
import java.util.Set;

import com.sessionintelligence.core.DetectionContext;
import com.sessionintelligence.core.Fingerprint;
import com.sessionintelligence.core.RequestObservation;
import com.sessionintelligence.core.SessionKey;
import com.sessionintelligence.core.SessionSnapshot;
import com.sessionintelligence.core.SnapshotUpdate;
import com.sessionintelligence.core.WindowSnapshot;

final class DetectorTestSupport {
    private DetectorTestSupport() {
    }

    static DetectionContext context(
            RequestObservation observation,
            SessionSnapshot previousSession,
            SessionSnapshot currentSession,
            WindowSnapshot previousWindow,
            WindowSnapshot currentWindow,
            Fingerprint fingerprint
    ) {
        SnapshotUpdate<SessionSnapshot> sessionUpdate = previousSession == null && currentSession == null
                ? null
                : new SnapshotUpdate<>(previousSession, currentSession);
        SnapshotUpdate<WindowSnapshot> windowUpdate = previousWindow == null && currentWindow == null
                ? null
                : new SnapshotUpdate<>(previousWindow, currentWindow);
        return new DetectionContext(observation, sessionUpdate, windowUpdate, fingerprint);
    }

    static RequestObservation observation(Instant timestamp, String path, SessionKey sessionKey) {
        return new RequestObservation(
                timestamp,
                "GET",
                path,
                200,
                null,
                "iphash",
                null,
                "uahash",
                "TestAgent",
                "1",
                "en-US",
                "gzip",
                Set.of("user-agent", "accept-encoding"),
                null,
                false,
                sessionKey
        );
    }
}
