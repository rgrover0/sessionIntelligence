package com.sessionintelligence.botsignals;

public interface Detector<TInput, TOutput> {
    TOutput detect(TInput input);
}
