# Local Testing and Demo Guide

This guide shows how to build the Phase 9 packaging locally, run a demo,
and observe session/window behavior and bot signals.

## 1) Build the starters locally

From repo root:
```bash
mvn -q -DskipTests install
```

This installs the four new modules into your local Maven repo:
- `window-session-core`
- `window-session-spring-boot-starter`
- `bot-signals-core`
- `bot-signals-spring-boot-starter`

## 2) Run the demo app

```bash
cd examples/bot-signals-demo-app
mvn -q spring-boot:run
```

The app runs on port 8080.

## 3) Postman (or curl) walkthrough

### A) Create a session
```bash
curl -i http://localhost:8080/start
```
Copy the `Set-Cookie` header (JSESSIONID).

### B) Same session with a windowName (tab)
```bash
curl -i http://localhost:8080/whoami \
  -H "Cookie: JSESSIONID=..." \
  -H "X-Window-Name: tab-1"
```
Expected response:
```json
{"sessionId":"<id>","windowName":"tab-1"}
```

### C) Same session without windowName
```bash
curl -i http://localhost:8080/whoami \
  -H "Cookie: JSESSIONID=..."
```
Expected response:
```json
{"sessionId":"<id>","windowName":null}
```

This shows one session with two different window contexts.

### D) Generate bot-like rate traffic
```bash
for i in {1..200}; do
  curl -s http://localhost:8080/rate \
    -H "Cookie: JSESSIONID=..." \
    -H "X-Window-Name: tab-1" > /dev/null
done
```

### E) Read signals
```bash
curl -s http://localhost:8080/signals/<sessionId> | jq
```
You will see a `SessionRiskScore` and any anomalies recorded.

## 4) What to look for in results

`SessionRiskScore` fields:
- `score` (0–100)
- `reasonCodes` (why the score increased)
- `evidenceSummary` (counts + timestamps only)

Risk bands:
- 0–20: LOW
- 21–50: MEDIUM
- 51–80: HIGH
- 81–100: CRITICAL

## 5) Tracing / OpenTelemetry

The bot signals starter exposes a `BotSignalsListener` hook. To add OTel
attributes, create a listener in your app:

```java
@Component
public class OtelBotSignalsListener implements BotSignalsListener {
  @Override
  public void onRiskScoreUpdated(SessionRiskScore score) {
    Span span = Span.current();
    if (!span.getSpanContext().isValid()) {
      return;
    }
    span.setAttribute("session.id", score.sessionKey().sessionId());
    span.setAttribute("risk.score", score.score());
    span.setAttribute("risk.reasons", score.reasonCodes().toString());
  }
}
```

Add dependency:
```xml
<dependency>
  <groupId>io.opentelemetry</groupId>
  <artifactId>opentelemetry-api</artifactId>
</dependency>
```

## 6) Pricing engine integration

The simplest integration is to publish the score keyed by sessionId
(visitor ID) to your pricing engine:

```java
public void onRiskScoreUpdated(SessionRiskScore score) {
  pricingPublisher.send(score.sessionKey().sessionId(), score.score(), score.reasonCodes());
}
```

This demo already stores the latest score in `SignalStore` and exposes it
via `/signals/{sessionId}`. That endpoint can be used to feed downstream
systems during testing.
