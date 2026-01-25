package com.sessionintelligence.starter;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.sessionintelligence.core.ObservationStore;
import com.sessionintelligence.core.RequestObservation;

public class InMemoryObservationStore implements ObservationStore {
    private final Queue<RequestObservation> observations = new ConcurrentLinkedQueue<>();

    @Override
    public void save(RequestObservation observation) {
        observations.add(observation);
    }
}
