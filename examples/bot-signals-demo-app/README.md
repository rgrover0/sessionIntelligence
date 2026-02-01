# Bot Signals Demo App

This demo app shows how `window-session-spring-boot-starter` and
`bot-signals-spring-boot-starter` work together.

## Run locally

From repo root:
```bash
mvn -q -DskipTests install
```

Then:
```bash
cd examples/bot-signals-demo-app
mvn -q spring-boot:run
```

## Endpoints
- `GET /start` — creates a session.
- `GET /whoami` — returns sessionId and windowName.
- `GET /rate` — sampled by bot signals.
- `GET /signals/{sessionId}` — returns latest risk score + anomalies.

## Postman / curl quick demo

1) Create a session:
```bash
curl -i http://localhost:8080/start
```
Copy the `Set-Cookie` value (JSESSIONID).

2) Same session with windowName:
```bash
curl -i http://localhost:8080/whoami \
  -H "Cookie: JSESSIONID=..." \
  -H "X-Window-Name: tab-1"
```

3) Same session without windowName:
```bash
curl -i http://localhost:8080/whoami \
  -H "Cookie: JSESSIONID=..."
```

4) Generate rate traffic (bot-like):
```bash
for i in {1..200}; do
  curl -s http://localhost:8080/rate \
    -H "Cookie: JSESSIONID=..." \
    -H "X-Window-Name: tab-1" > /dev/null
done
```

5) Fetch signals:
```bash
curl -s http://localhost:8080/signals/{sessionId} | jq
```

Use the `sessionId` from `/whoami` or `/start`.
