package com.sessionintelligence.starter;

import java.util.List;

import com.sessionintelligence.core.AnomalyDetector;
import com.sessionintelligence.core.AnomalyEvent;
import com.sessionintelligence.core.RequestObservation;
import com.sessionintelligence.core.SessionSnapshot;
import com.sessionintelligence.core.WindowSnapshot;

public class NoOpAnomalyDetector implements AnomalyDetector {
    @Override
    public List<AnomalyEvent> detect(
            RequestObservation observation,
            SessionSnapshot sessionSnapshot,
            WindowSnapshot windowSnapshot
    ) {
        return List.of();
    }
}
