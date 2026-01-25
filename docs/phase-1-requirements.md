# Phase 1: Requirements Decomposition

## 1.1 Definitions

### Session
- The HTTP session tracked by Spring Session (backed by Infinispan/Redis/etc.).
- Must remain compatible with Spring Boot 3.x and Spring Security 6.x.

### Window Scope
- A "tab journey" identified by `windowName` (header) and a cached `numericId`.
- Implemented in the Spring application via custom scope configuration.
- Classes created within this scope are scoped to the window journey.

### SessionKey Model
- `sessionId`
- `windowName`
- Optional: `tenantId` / `appId` for multi-app gateway deployments

## 1.2 Observables

### Request-level
- Timestamp, method, path template, status code
- Client IP (consider proxies), XFF chain (sanitized), ASN (optional later)
- User-Agent string + parsed UA family
- Accept-Language, Accept-Encoding
- Header presence patterns (bots are often odd)
- `windowName` header value
- Referer/Origin presence and consistency (CORS vs navigation)

### Session-level
- Session creation time, last access time (from Spring Session if available)
- Session attribute keys used (not values; only if allowed by policy)

### Window-level
- Journey start time
- Action count
- Rate lookup frequency
- Location search diversity and velocity
- Parallel windows count

## 1.3 Anomaly Types (Explicit Outcomes)

### A) Session Reuse Anomalies
- Same `sessionId` observed from "impossible" IP/UA changes within short time
- Same `sessionId` used concurrently across pods with conflicting fingerprints
- High request concurrency for one `sessionId` (especially rate endpoints)
- Optional whitelist when:
  - Reservation / purchase completes
  - Useful contact info is provided (configurable flag/input)

### B) Cross-Pod Session Resurrection
- Session `lastAccessTime` goes backwards
- Session appears "new" in a pod but already had significant history
- Session attributes missing suddenly then reappear (ghost/hydration mismatch)

### C) Window Scope Collisions
- Same `sessionId` + `windowName` reused across separate clients (IP/UA mismatch)
- Multiple different `windowName` values reused in mechanical patterns
- `windowName` absent or constantly changing per request (automation)

### D) Bot Scraping Indicators (Signal-only)
- Extreme request rate to rate endpoints
- High-entropy location permutations
- Low think-time variance (too consistent)
- Missing browser headers typical of real browsers
- Unrealistically high number of parallel windows
- Pixel size too small for desktop/laptop hardware class
- Specific cookie key missing or showing repetitive patterns
