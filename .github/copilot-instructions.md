# Copilot Instructions for Loterias API

## Overview
**Loterias API** is a Spring Boot application that fetches Brazilian lottery (CAIXA) results, stores them in MongoDB, and serves them via REST endpoints. It runs on **Java 17** with **Spring Boot 2.4.2** and uses Maven for builds.

## Architecture

### Core Data Flow
1. **ScheduledConsumer** (multiple daily cron schedules, Brazil timezone) → 
2. **LoteriasUpdate** (orchestrates updates for all lotteries) → 
3. **Consumer** (fetches from CAIXA API via jsoup) → 
4. **ResultadoService** (processes/caches via MongoDB) → 
5. **ApiRestController** (exposes REST endpoints)

### Key Components
- **Consumer.java**: Fetches lottery data from `https://servicebus2.caixa.gov.br/portaldeloterias/api/{loteria}/{concurso}`, parses JSON responses, handles IP blocking
- **ResultadoService.java**: Handles MongoDB queries, implements `@Cacheable("resultados")` for performance, sorts results by `concurso` descending
- **ApiRestController.java**: REST layer with endpoints like `GET /api/{loteria}/latest`, validates lottery names against `Loteria` enum (10 supported lotteries)
- **ScheduledConsumer.java**: Multiple daily triggers (noon, 9pm, 9:15pm, 10pm, 11:10pm, midnight, 1am) on Mon-Sat, São Paulo timezone
- **LoteriasUpdate.java**: Async batch updater that detects new concursos and fills gaps, clears cache after each update

### Data Model
- **Resultado.java**: MongoDB document (collection="resultados"), composite ID (`ResultadoId`: loteria + concurso number), includes dezenas, premiações, winner locations
- **Loteria.java**: Enum of 10 supported lotteries (MAIS_MILIONARIA, MEGA_SENA, LOTOFACIL, QUINA, LOTOMANIA, TIMEMANIA, DUPLA_SENA, FEDERAL, DIA_DE_SORTE, SUPER_SETE)

## Critical Knowledge: IP Blocking & HTTP Layer

### The Problem
CAIXA's WAF (Web Application Firewall) blocks cloud provider IPs (e.g., Render's `74.220.49.25`) with HTTP 403 responses, while allowing personal/residential IPs. This is **NOT** a retry-able issue—all requests from blocked IPs will fail.

### Solution Architecture
1. **CaixaApiBlockedException**: New exception extending `IOException` with `isIpBlocking()` and `getHttpStatus()` flags (file: [src/main/java/com/gutotech/loteriasapi/model/exception/CaixaApiBlockedException.java](src/main/java/com/gutotech/loteriasapi/model/exception/CaixaApiBlockedException.java))
2. **SSLHttpConnectionService**: Retry logic that **skips retries for 403 blocking errors** but retries transient network failures (exponential backoff: 2000ms base delay, configurable via `caixa.api.retry.*` properties)
3. **SSLHelper.java**: Comprehensive browser header spoofing (Chrome 124+) including `Sec-Fetch-*`, `Sec-Ch-Ua`, Origin headers to bypass bot detection
4. **ExceptionsHandler.java**: Returns HTTP 503 (not 400) for `CaixaApiBlockedException` with hint "contact CAIXA to whitelist"

### HTTP Headers Strategy
Added/updated in `SSLHelper.getConnection()`:
- **Security headers**: `Sec-Fetch-Dest`, `Sec-Fetch-Mode`, `Sec-Fetch-Site`, `Origin`
- **Client hints**: `Sec-Ch-Ua`, `Sec-Ch-Ua-Mobile`, `Sec-Ch-Ua-Platform` (modern Chrome fingerprint)
- **Compression**: `Accept-Encoding: gzip, deflate, br`
- **Connection control**: `Connection: keep-alive`, `TE: trailers`
- **Timeout**: 25000ms, `followRedirects(true)`, `maxBodySize(0)`

### Configuration
- **Dev** ([application.properties](src/main/resources/application.properties)): 3 retries, 2000ms delay
- **Prod** ([application-prod.properties](src/main/resources/application-prod.properties)): 5 retries, 3000ms delay

## Developer Workflows

### Build & Run
```bash
# Clean build with tests
./mvnw clean install

# Run locally (port 8090)
./mvnw spring-boot:run

# Quick build (skip tests)
./mvnw clean package -DskipTests

# Run via Windows batch
./_startServerLocal.bat
```

### Testing
```bash
./mvnw test                           # Run all tests
./mvnw test -Dtest=ConsumerTest      # Run specific test class
./mvnw test -Dtest=ConsumerTest#testMethod  # Run specific test method
```

### Debugging
1. Set breakpoints in [Consumer.java](src/main/java/com/gutotech/loteriasapi/consumer/Consumer.java) (API calls), [ResultadoService.java](src/main/java/com/gutotech/loteriasapi/service/ResultadoService.java) (cache/queries), or [ApiRestController.java](src/main/java/com/gutotech/loteriasapi/rest/ApiRestController.java) (REST handlers)
2. Run in IDE debug mode to step through lottery data parsing (jsoup Document → JSON → Resultado model)
3. Check logs via SLF4J (classes use `LoggerFactory.getLogger()` for all logging)

### Key Endpoints (for manual testing)
```bash
GET /api                               # List all 10 supported lotteries
GET /api/megasena                     # All Mega-Sena results (cached)
GET /api/megasena/latest              # Latest Mega-Sena result
GET /api/megasena/2620                # Specific concurso #2620
```

## Project-Specific Patterns & Conventions

### Logging
- Uses **SLF4J** with logger: `LoggerFactory.getLogger(ClassName.class)`
- Consumer logs: "Fetching from CAIXA: {url}", "CAIXA response for {} | len={}", "[Attempt X/Y]" format
- Service/Controller: Info for operations, Error for failures
- Do NOT use `System.out.println()`

### Exception Handling
- Throw `CaixaApiBlockedException` for 403 IP blocking (DO NOT retry)
- Throw generic `IOException` for transient network errors (retry with backoff)
- Throw `RuntimeException("CAIXA returned non-JSON response")` for parsing failures
- REST layer catches exceptions via `ExceptionsHandler.java`, returns appropriate HTTP codes (503 for blocking, 400 for client errors, 404 for not found)

### Caching
- `@Cacheable("resultados")` on `ResultadoService.findByLoteria()` (Spring Boot caching abstraction)
- Cache is manually cleared in `LoteriasUpdate.checkForUpdates()` after batch updates (ensures fresh data after scheduled fetches)
- Cache manager injected via `@Autowired private CacheManager cacheManager`

### Asynchronous Processing
- `LoteriasUpdate` uses `@Async` inner class `LoteriaUpdateTask` to process lottery updates concurrently (one thread per lottery)
- `@EnableAsync` + `@Component` on inner class enables async task scheduling
- Exception handling inside async tasks logs errors but doesn't fail the batch (graceful degradation per lottery)

### Validation
- All incoming lottery names are validated against `Loteria.asList()` enum in controller
- Invalid names return HTTP 400 with message: "'%s' não é o id de nenhuma das loterias suportadas"
- Concurso not found returns HTTP 404 with appropriate message

### Data Synchronization Pattern
1. **On app startup** (ApplicationRunner): `LoteriasUpdate.checkForUpdates()` runs immediately
2. **Scheduled daily**: ScheduledConsumer triggers at 6 times per day (Mon-Sat, São Paulo time)
3. **Incremental sync**: LoteriasUpdate detects latest remote concurso, fills any gaps from local max up to remote
4. **Gap-filling retries**: Failed intermediate concursos are retried 3 times with exponential backoff before logging and skipping
5. **Cache invalidation**: After sync completes, entire "resultados" cache is cleared

## Integration Points & External Dependencies

### CAIXA API
- **Base URL**: `https://servicebus2.caixa.gov.br/portaldeloterias/api/`
- **Endpoints**: `/{loteria}` (latest), `/{loteria}/{concurso}` (specific result)
- **Response Format**: JSON with fields: `numero`, `dataApuracao`, `localSorteio`, `nomeMunicipioUFSorteio`, `dezenasSorteadasOrdemSorteio`, `premiacoes`, etc.
- **Status**: Subject to IP blocking; headers must mimic Chrome browser

### MongoDB
- **Connection**: Spring Data MongoDB auto-configured (default: localhost:27017, configurable via properties)
- **Database/Collection**: Auto-determined by Spring, collection name is "resultados"
- **Repository**: `ResultadoRepository` provides custom queries like `findById_Loteria()`, `findTopById_Loteria()`

### Spring Features Used
- **Spring Web**: REST controller, ResponseEntity
- **Spring Data MongoDB**: Repository pattern, auto schema creation
- **Spring Cache**: `@Cacheable`, CacheManager for manual invalidation
- **Spring Scheduling**: `@Scheduled` cron expressions, timezone support
- **Spring Async**: `@Async` for concurrent lottery updates
- **Swagger/Springfox**: Auto-generated API docs (SwaggerConfig.java)

## Examples: Common Tasks

### Adding a New Endpoint
1. Add method to [ApiRestController.java](src/main/java/com/gutotech/loteriasapi/rest/ApiRestController.java) with `@GetMapping` or appropriate verb
2. Validate input against `Loteria.asList()` (or use custom validator)
3. Call `resultadoService` for data retrieval
4. Return `ResponseEntity.ok(data)` or throw checked exception (ExceptionsHandler catches it)
5. Add `@ApiOperation` Swagger documentation

### Debugging a Fetch Failure
1. Check logs for "[Attempt X/Y]" and status code
2. If HTTP 403 with "Your IP" → IP is blocked (contact CAIXA)
3. If HTTP 4xx/5xx → Check CAIXA API status
4. If non-JSON response → WAF returned HTML (likely bot detection; verify headers in SSLHelper)
5. If timeout → Increase `caixa.api.retry.initial-delay-ms` in application properties

### Modifying Retry Strategy
1. Edit [application.properties](src/main/resources/application.properties) (dev) or [application-prod.properties](src/main/resources/application-prod.properties) (production)
2. Update `caixa.api.retry.max-attempts` (recommended: 3 for dev, 5 for prod)
3. Update `caixa.api.retry.initial-delay-ms` (recommended: 2000 for dev, 3000 for prod)
4. Backoff calculation: `delay * Math.pow(2, attemptNumber - 1)` (exponential)

### Adding a New Lottery
1. Add enum constant to [Loteria.java](src/main/java/com/gutotech/loteriasapi/model/Loteria.java) (e.g., `NEW_LOTTERY("newlottery")`)
2. Verify CAIXA API supports the endpoint
3. Update Swagger `ALLOWABLE_VALUES` string in [ApiRestController.java](src/main/java/com/gutotech/loteriasapi/rest/ApiRestController.java)
4. Run `./mvnw clean install` and test: `GET /api/newlottery/latest`

## Files You'll Modify Most
- [Consumer.java](src/main/java/com/gutotech/loteriasapi/consumer/Consumer.java) - CAIXA parsing logic
- [ResultadoService.java](src/main/java/com/gutotech/loteriasapi/service/ResultadoService.java) - Business queries
- [ApiRestController.java](src/main/java/com/gutotech/loteriasapi/rest/ApiRestController.java) - REST endpoints
- [application.properties](src/main/resources/application.properties) - Configuration & retry settings
- [SSLHelper.java](src/main/java/com/gutotech/loteriasapi/util/SSLHelper.java) - HTTP headers for CAIXA requests

## Quick Reference: Supported Lotteries
`maismilionaria`, `megasena`, `lotofacil`, `quina`, `lotomania`, `timemania`, `duplasena`, `federal`, `diadesorte`, `supersete`
