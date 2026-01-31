package com.sessionintelligence.botsignals;

public record SnapshotUpdate<T>(T previous, T current) {
}
