# HTTP Layer Improvements for Render Cloud Deployment

## Problem
- **Local**: Requests to `https://servicebus2.caixa.gov.br/portaldeloterias/api/...` return valid JSON
- **Render (Cloud)**: Same requests receive HTML or 403 responses → JSONObject parsing fails
- **Root Cause**: Bot/cloud detection by remote server - missing critical browser headers

## Solution Overview
Modified HTTP layer to "humanize" requests by adding all modern Chrome browser headers that cloud WAFs check.

## Changes Made

### 1. **SSLHelper.java** - Enhanced `getConnection()` method
Added comprehensive browser headers across 5 categories:

#### A. Accept Headers (Critical)
```java
.header("Accept", "application/json, text/plain, */*")
.header("Accept-Encoding", "gzip, deflate, br")  // NEW - Enable compression
.header("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7")
```

#### B. Security Headers (NEW - Bypass WAF detection)
```java
.header("Sec-Fetch-Dest", "empty")           // NEW
.header("Sec-Fetch-Mode", "cors")            // NEW
.header("Sec-Fetch-Site", "cross-site")      // NEW
.header("Origin", "https://loterias.caixa.gov.br")  // NEW
```

#### C. Client Hints (NEW - Modern browser identification)
```java
.header("Sec-Ch-Ua", "\"Not A(Brand\";v=\"99\", \"Google Chrome\";v=\"124\", \"Chromium\";v=\"124\"")  // NEW
.header("Sec-Ch-Ua-Mobile", "?0")            // NEW
.header("Sec-Ch-Ua-Platform", "\"Windows\"") // NEW
```

#### D. Connection Control
```java
.header("Cache-Control", "no-cache")
.header("Pragma", "no-cache")
.header("Connection", "keep-alive")
.header("TE", "trailers")  // NEW - Trailer support
```

#### E. Configuration Updates
```java
.timeout(25000)              // UPDATED: 20000 → 25000ms
.followRedirects(true)       // NEW - Handle redirects transparently
.maxBodySize(0)              // NEW - Allow large responses
.userAgent("...Chrome/124.0.0.0...")  // UPDATED: 120 → 124 (current)
```

### 2. **SSLHttpConnectionService.java** - Enhanced logging & error handling
```java
// Added SLF4J logger (replaces System.out.println)
// Enhanced response diagnostics with:
// - Status code logging
// - Response headers logging (debug level)
// - Non-JSON detection with first 200 chars
// - HTTP error detection (4xx, 5xx)
```

## Why These Changes Work

| Header | Purpose | Cloud WAF Impact |
|--------|---------|-----------------|
| `Sec-Fetch-*` | Indicates legitimate browser behavior | Distinguishes browsers from bots |
| `Sec-Ch-Ua` | Identifies Chrome explicitly | Matches real browser fingerprint |
| `Accept-Encoding` | Tells server we handle compression | Shows browser capability parity |
| `Origin` | Cross-origin request legitimacy | Prevents origin validation rejection |
| `followRedirects(true)` | Handle auth/landing redirects | Navigates around bot barriers |

## Testing Strategy

1. **Local verification** (should still work):
   ```bash
   ./mvnw clean install
   ./mvnw test
   ```

2. **Cloud testing** (Render):
   - Deploy and monitor logs for `CAIXA Response: status=200`
   - Verify response content is JSON (`bodyLength > 0` and no "HTML" warning)
   - Check that `valueAcumulado*` fields are populated (proving JSON was parsed)

3. **Debug output** (if needed):
   - Set logging level to DEBUG to see full response headers
   - Inspect first 200 chars of any non-JSON responses

## Files Modified
- [SSLHelper.java](src/main/java/com/gutotech/loteriasapi/util/SSLHelper.java)
- [SSLHttpConnectionService.java](src/main/java/com/gutotech/loteriasapi/consumer/SSLHttpConnectionService.java)

## Rollback Plan
If issues occur, revert both files to previous commit - changes are isolated to HTTP configuration layer.
