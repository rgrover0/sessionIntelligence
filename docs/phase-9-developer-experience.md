# Phase 9: Packaging and Developer Experience

## What it does
- Captures request observations without changing application logic.
- Computes session/window snapshots and emits anomalies and risk scores.
- Provides telemetry hooks (logs, metrics, optional OTel attributes).

## What it does not do
- Does not block traffic or enforce actions.
- Does not store anything in the HTTP session besides reading the sessionId.
- Does not implement Infinispan/Redis integrations yet (stubs only).

## Modules
- `session-intelligence-core`: shared contracts and engine.
- `session-intelligence-spring-boot-starter`: auto-configuration, filters, in-memory store.
- `session-intelligence-store-infinispan`: optional Infinispan store (stub).
- `session-intelligence-otel`: optional OpenTelemetry listener.

## Enable in Spring Boot

Maven dependency (starter):
```xml
<dependency>
  <groupId>com.sessionintelligence</groupId>
  <artifactId>spring-boot-starter-session-intelligence</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Optional modules:
```xml
<dependency>
  <groupId>com.sessionintelligence</groupId>
  <artifactId>session-intelligence-store-infinispan</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
<dependency>
  <groupId>com.sessionintelligence</groupId>
  <artifactId>session-intelligence-otel</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Configuration examples

Minimal in-memory config:
```yaml
session-intelligence:
  enabled: true
  include-path-patterns:
    - "/rate"
  headers:
    window-name: "X-Window-Name"
  storage:
    backend: IN_MEMORY
```

Enable OTel attributes:
```yaml
session-intelligence:
  telemetry:
    otel-enabled: true
```

Infinispan (stub, no implementation yet):
```yaml
session-intelligence:
  storage:
    backend: INFINISPAN_REMOTE
    telemetry-cache-name: "session-intelligence-telemetry"
```

PII guardrails:
```yaml
session-intelligence:
  privacy:
    store-raw-ip: false
    store-raw-user-agent: false
    ip-signals-enabled: false
    retention-ttl: 24h
```

## Interpreting SessionRiskScore
- Score range: 0–100.
- Risk band mapping:
  - 0–20: LOW
  - 21–50: MEDIUM
  - 51–80: HIGH
  - 81–100: CRITICAL
- Reason codes explain why the score was raised.
- Evidence summary contains counts and timestamps only (no PII).

## Operational playbook (security team)

Alerts and dashboards:
- Alert on HIGH/CRITICAL bands.
- Track anomalies per type (session resurrection, window collision).
- Monitor rate endpoint rate per session/window.
- Monitor windows per session for bursty automation.

Thresholds:
- Tune request-rate thresholds based on typical peak load.
- Adjust window explosion threshold for multi-tab usage.
- Set fingerprint drift window to reflect normal UA changes.

Response actions:
- Use risk score changes to trigger manual review.
- For consistent automation, escalate to fraud tooling.
- Keep allowlists external; the library remains signal-only.

## Example app
See `examples/session-intelligence-sample-app`:
- `/start` creates a session.
- `/rate` uses `X-Window-Name` to simulate a tab journey.
- `application.yml` shows in-memory configuration.
- `application-infinispan.yml` shows optional Infinispan backend config.
