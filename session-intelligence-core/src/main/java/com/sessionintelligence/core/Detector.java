package com.sessionintelligence.core;

public interface Detector<TInput, TOutput> {
    TOutput detect(TInput input);
}
