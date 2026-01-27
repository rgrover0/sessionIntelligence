package com.sessionintelligence.core;

public record SnapshotUpdate<T>(T previous, T current) {
}
