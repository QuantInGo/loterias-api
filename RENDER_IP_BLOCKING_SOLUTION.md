# Render IP Blocking Issue - Solution Summary

## Problem
When running on Render cloud, the application receives HTTP 403 responses from CAIXA API with IP blocking messages:
```
"Your IP74.220.49.25"
"HTTP 403 - IP Blocking by CAIXA WAF"
```

This is a **firewall-level IP blocking by CAIXA**, not a bot detection issue that can be bypassed with headers.

## Root Cause
- CAIXA's WAF blocks cloud provider IPs (including Render's `74.220.49.25`)
- Locally it works because your personal IP is likely whitelisted or not blocked
- This is a deployment infrastructure issue, not a code issue

## Solution Implemented

### 1. New Exception Type: `CaixaApiBlockedException`
- Extends `IOException` for proper exception hierarchy
- Distinguishes IP blocking errors from transient network errors
- Signals to clients that this is an external, persistent issue

**File**: [src/main/java/com/gutotech/loteriasapi/model/exception/CaixaApiBlockedException.java](src/main/java/com/gutotech/loteriasapi/model/exception/CaixaApiBlockedException.java)

### 2. Enhanced Error Handling in `SSLHttpConnectionService`
- Detects IP blocking (403 + "Your IP" in response)
- **Does NOT retry** IP blocking errors (no point, they'll all fail)
- **Continues retrying** for transient network errors
- Logs attempt numbers for debugging

**File**: [src/main/java/com/gutotech/loteriasapi/consumer/SSLHttpConnectionService.java](src/main/java/com/gutotech/loteriasapi/consumer/SSLHttpConnectionService.java)

### 3. Better Exception Handler
- Returns **HTTP 503 (Service Unavailable)** instead of 400 for blocking errors
- Signals to clients that the issue is external and temporary
- Includes hint: "contact CAIXA to whitelist"
- Other errors still return 400

**File**: [src/main/java/com/gutotech/loteriasapi/rest/exception/ExceptionsHandler.java](src/main/java/com/gutotech/loteriasapi/rest/exception/ExceptionsHandler.java)

### 4. Enhanced HTTP Headers in `SSLHelper`
Added:
- `DNT: 1` - Do Not Track signal
- `Upgrade-Insecure-Requests: 1` - Preference for secure connections

**File**: [src/main/java/com/gutotech/loteriasapi/util/SSLHelper.java](src/main/java/com/gutotech/loteriasapi/util/SSLHelper.java)

### 5. Configuration for Retry Strategy
**Dev** (`application.properties`):
```properties
caixa.api.retry.max-attempts=3
caixa.api.retry.initial-delay-ms=2000
```

**Prod** (`application-prod.properties`):
```properties
caixa.api.retry.max-attempts=5
caixa.api.retry.initial-delay-ms=3000
```

## Expected Behavior After Deploy

### On Localhost (works as before):
```json
GET /api/test/timemania
→ 200 OK
→ Returns full lottery results
```

### On Render (now returns 503 instead of 400):
```json
GET /api/test/timemania
→ 503 Service Unavailable
{
  "status": 503,
  "error": "Service Unavailable",
  "message": "CAIXA returned HTTP 403 - IP Blocking by WAF (Cloud IP blocking - contact CAIXA to whitelist)",
  "timestamp": 1769626090278,
  "path": "/api/test/timemania"
}
```

## To Fix This Permanently

You need to **contact CAIXA** and request that they whitelist the Render IP:
- **IP Address**: `74.220.49.25`
- **Request**: Whitelist this IP for `https://servicebus2.caixa.gov.br/portaldeloterias/api/`

OR use a workaround:
1. **Proxy API**: Route through a residential proxy service
2. **Headless Browser**: Use Selenium/Playwright for JavaScript-capable requests
3. **Alternative Source**: Find another data provider
4. **Cache Aggressively**: Serve stale cached data during outages

## Files Modified
1. ✅ [CaixaApiBlockedException.java](src/main/java/com/gutotech/loteriasapi/model/exception/CaixaApiBlockedException.java) - NEW
2. ✅ [SSLHttpConnectionService.java](src/main/java/com/gutotech/loteriasapi/consumer/SSLHttpConnectionService.java)
3. ✅ [ExceptionsHandler.java](src/main/java/com/gutotech/loteriasapi/rest/exception/ExceptionsHandler.java)
4. ✅ [SSLHelper.java](src/main/java/com/gutotech/loteriasapi/util/SSLHelper.java)
5. ✅ [Consumer.java](src/main/java/com/gutotech/loteriasapi/consumer/Consumer.java)
6. ✅ [application.properties](src/main/resources/application.properties)
7. ✅ [application-prod.properties](src/main/resources/application-prod.properties)

## Testing
- **Local**: Run `.\mvnw.cmd spring-boot:run` - should still work
- **Render**: Deploy and check logs for clear "IP Blocking" message
- **Client**: Will receive 503 instead of 400, which properly signals the issue is external
