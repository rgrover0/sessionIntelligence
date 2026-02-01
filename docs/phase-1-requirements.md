# Phase 1: Requirements Decomposition (Session-Intelligence Starter)

This document turns the Phase 1 problem statement into an explicit,
implementable spec for the Session-Intelligence Spring Boot starter.

## 1.1 Session and Window concepts

### Session (server-side)
The HTTP session managed by Spring Session (e.g., Infinispan, Redis,
JDBC). The library observes session identity and metadata but does not
replace storage or lifecycle management.

### Window scope (tab journey)
An application-defined scope that represents a single browser tab
journey. It is identified by:
- `windowName` header value (required for a stable window identity)
- `cacheNumericId` (if the application uses this to disambiguate tab
  identity)

Window-scoped classes are created by the application's custom scope
configuration. The library treats window scope as an observed dimension
and does not register or manage Spring scopes itself.

### SessionKey model
The identity used by the library for correlating signals:
- `sessionId` (required)
- `windowName` (required; empty value is treated as "missing")
- `tenantId` or `appId` (optional; for multi-app gateway deployments)

## 1.2 Observables (privacy-aware, stable data)

### Request-level
- `timestamp`, HTTP `method`, path template, response `statusCode`
- Client IP (proxy-aware), sanitized X-Forwarded-For chain
- User-Agent string and parsed UA family
- `accept-language`, `accept-encoding`
- Header presence patterns (missing/extra headers)
- `windowName` header value
- `referer`/`origin` presence and consistency
- Optional (future): ASN derived from IP

### Session-level
- Session creation time and last access time (from Spring Session)
- Session attribute keys accessed (keys only, not values)

### Window-level
- Journey start time
- Action count and request rate
- Location/search diversity and velocity
- Parallel window count (per session)

Notes:
- Do not persist sensitive attribute values.
- Any optional collection must be explicitly enabled by configuration.

## 1.3 Anomaly types (explicit detection outcomes)

Each anomaly emits a signal with context, and does not block traffic.

### A) Session reuse anomalies
Signals that the same session identity is being reused unnaturally.

Measurable definitions:
- Same `sessionId` observed with "impossible" IP/UA changes within a
  short interval (configurable threshold).
- Same `sessionId` active concurrently across pods with conflicting
  fingerprints.
- Excessive request concurrency for one `sessionId` on rate-sensitive
  endpoints.

Whitelisting guidance:
- If checkout or reservation creation is detected, optionally suppress
  session reuse signals for that `sessionId`.
- If a verified contact info flag is present in a configurable request
  marker (e.g., header or session attribute), optionally suppress.

### B) Cross-pod session resurrection
Signals indicating inconsistent session history across pods.

Measurable definitions:
- Session last-access time moves backwards.
- Session appears "new" in a pod but already has significant history in
  the library store.
- Session attribute keys vanish and reappear abruptly.

### C) Window scope collisions
Signals indicating the same window identity is used by multiple clients.

Measurable definitions:
- Same `sessionId` + `windowName` reused with IP/UA mismatch.
- `windowName` absent or changes every request (automation pattern).
- Multiple distinct `windowName` values reused in an overly regular,
  mechanical pattern.

### D) Bot scraping indicators (signal-only)
Signals suggesting automation or scraping behavior.

Measurable definitions:
- Extreme request rates to rate endpoints.
- High entropy location permutations within short time spans.
- Very low variance in think-time between requests.
- Missing browser headers typical of real browsers.
- Unrealistically high number of parallel windows.
- Browser pixel size too small for declared device class.
- Required cookie key missing or containing a repetitive pattern.

