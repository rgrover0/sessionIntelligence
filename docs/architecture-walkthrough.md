# Architecture Walkthrough (Code-Level)

This document is a code-centric walkthrough of how the Session
Intelligence library is constructed and how requests flow through it.
It is intended for developers integrating or extending the library.

## Module layout

- `session-intelligence-core`
  - Shared contracts and the engine pipeline.
- `session-intelligence-spring-boot-starter`
  - Spring Boot auto-configuration, filter, in-memory store, detectors,
    and default scoring.
- `session-intelligence-store-infinispan` (optional)
  - Stub Infinispan store + auto-configuration.
- `session-intelligence-otel` (optional)
  - OTel listener + auto-configuration.
- `examples/session-intelligence-sample-app`
  - Minimal application showing usage.

## High-level data flow

```
HTTP request
   |
   v
SessionIntelligenceFilter (starter)
   - validates windowName
   - normalizes headers
   - collects observation
   |
   v
SessionIntelligenceEngine (core)
   1) record session/window snapshots (ObservationStore)
   2) run detectors (ObservationDetector)
   3) emit anomaly events (SessionIntelligenceListener)
   4) compute risk score (RiskScorer)
   5) emit risk score update (SessionIntelligenceListener)
   |
   v
Listeners (starter + optional modules)
   - publish Spring events
   - emit logs and metrics
   - add OTel span attributes (optional)
```

## Core module walkthrough

### RequestObservation
`RequestObservation` is the single input to the engine and contains:
- request metadata (method, path, status)
- session key (sessionId + windowName)
- PII-safe fields (hashed IP/UA) and parsed UA family/major
- header names and auth presence

### ObservationStore
`ObservationStore` combines three store concerns:
- `recordSession(...)` for `SessionSnapshot`
- `recordWindow(...)` for `WindowSnapshot`
- `save(SessionRiskScore)` for risk output

Snapshots are small DTOs with counts and timestamps only.

### Engine pipeline
`SessionIntelligenceEngine.observe(...)`:
1. `FingerprintStrategy` computes a stable hash
2. `ObservationStore` records session/window updates
3. `ObservationDetector` list produces `DetectorFinding` objects
4. `AnomalyEvent` emitted for each finding
5. `RiskScorer` returns a `SessionRiskScore` for the observation

The engine never blocks requests; listeners are best‑effort.

## Starter module walkthrough

### Filter
`SessionIntelligenceFilter` is the integration point. It:
- reads `windowName` from a header
- reads sessionId via `request.getSession(false)` (no session creation)
- validates and normalizes input (safety guardrails)
- respects sampling and include/exclude path patterns
- builds `RequestObservation` and calls the engine

### Default storage
`InMemoryObservationStore`:
- records rolling aggregates per session/window
- enforces retention TTL and window count caps
- does not store raw PII by default

### Detectors
Each detector implements `ObservationDetector`:
- `FingerprintDriftDetector`
- `RequestRateDetector`
- `WindowExplosionDetector`
- `WindowCollisionDetector`
- `SessionResurrectionDetector`

Each returns `DetectorFinding` with severity, reason code, and evidence.

### Scoring
`WeightedRiskScorer`:
- combines findings into a 0–100 score
- applies severity multipliers and decay window
- returns `SessionRiskScore` with reason codes and evidence summary

### Telemetry
`PublishingSessionIntelligenceListener`:
- publishes Spring events
- emits structured logs (no PII)
- emits metrics for scores/anomalies/rates/windows

## Optional modules

### Infinispan store
`session-intelligence-store-infinispan` contributes:
- `InfinispanRemoteObservationStore` stub
- auto-config that activates when
  `session-intelligence.storage.backend=INFINISPAN_REMOTE`

The actual storage implementation is deferred and will require a
proto schema for DTOs.

### OpenTelemetry
`session-intelligence-otel` contributes:
- `OtelSessionIntelligenceListener`
- auto-config activated when `telemetry.otel-enabled=true`

Adds span attributes:
- `session.id.hash`
- `window.name.hash`
- `risk.score`
- `anomaly.codes`

## Example app

`examples/session-intelligence-sample-app` provides:
- `/start` to create a session
- `/rate` that consumes `X-Window-Name`
- `application.yml` with in-memory config
- `application-infinispan.yml` for remote store

## Extension points

Common extension points are:
- Provide custom `ObservationStore` for production.
- Provide custom `ObservationDetector` implementations.
- Provide a custom `RiskScorer` or `SessionActionAdvisor`.
- Implement `SessionIntelligenceListener` for external systems.
