package com.sessionintelligence.core;

import java.util.List;

public interface AnomalyDetector {
    List<AnomalyEvent> detect(
            RequestObservation observation,
            SessionSnapshot sessionSnapshot,
            WindowSnapshot windowSnapshot
    );
}
