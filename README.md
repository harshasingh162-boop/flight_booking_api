# Flight Booking API

An in-memory REST API for registering flights and booking seats.

## Tech stack

| | |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.0.6 |
| Validation | Jakarta Bean Validation |

## Run

```bash
./mvnw spring-boot:run
```

Starts on **http://localhost:8080**.

---

## API reference

### POST /flights — Register a flight

**Request body**

```json
{ "flightNumber": "string (non-blank)", "capacity": "integer (> 0)" }
```

| Status | Meaning |
|---|---|
| 201 Created | Flight registered |
| 400 Bad Request | `flightNumber` is blank or `capacity` ≤ 0 |
| 409 Conflict | A flight with that `flightNumber` already exists |

---

### POST /flights/{flightNumber}/bookings — Book a seat

**Request body**

```json
{ "passengerName": "string (non-blank)" }
```

**Response body (201)**

```json
{ "bookingId": "uuid", "flightNumber": "string" }
```

| Status | Meaning |
|---|---|
| 201 Created | Seat reserved; response contains `bookingId` |
| 400 Bad Request | `passengerName` is blank |
| 404 Not Found | No flight exists with that `flightNumber` |
| 409 Conflict | Flight is full; no seat was consumed |

---

## Examples

The four commands below tell an end-to-end story: create a flight with capacity 2, book both seats, then hit the 409.

**1. Create flight AA100 with 2 seats**

```bash
curl -s -X POST http://localhost:8080/flights \
  -H "Content-Type: application/json" \
  -d '{"flightNumber":"AA100","capacity":2}'
```

**2. Book the first seat**

```bash
curl -s -X POST http://localhost:8080/flights/AA100/bookings \
  -H "Content-Type: application/json" \
  -d '{"passengerName":"Alice"}'
```

```json
{"bookingId":"<uuid>","flightNumber":"AA100"}
```

**3. Book the second (last) seat**

```bash
curl -s -X POST http://localhost:8080/flights/AA100/bookings \
  -H "Content-Type: application/json" \
  -d '{"passengerName":"Bob"}'
```

```json
{"bookingId":"<uuid>","flightNumber":"AA100"}
```

**4. Attempt a third booking — flight is full (409)**

```bash
curl -s -o /dev/null -w "%{http_code}" \
  -X POST http://localhost:8080/flights/AA100/bookings \
  -H "Content-Type: application/json" \
  -d '{"passengerName":"Carol"}'
```

```
409
```

## What I'd improve with more time

### Persistence & data model
- **Durable storage.** State lives in an in-memory `ConcurrentHashMap` and is lost on restart. I'd move flights and bookings to a database (Postgres + JPA) so bookings survive restarts and the service can scale beyond one instance.
- **Seat-level inventory.** A flight is modelled as a capacity counter, not a seat map. Real bookings are seat-specific (e.g. 12A), so I'd model individual seats, which also enables seat selection and preferences.
- **Group bookings.** A request books exactly one seat. Multi-seat bookings introduce all-or-nothing vs. best-effort semantics when fewer seats remain than requested; I'd design that explicitly and make the multi-seat reservation atomic.
- **Cancellation.** There's no way to release a seat. A cancellation endpoint would need the same atomicity guarantees as booking.

### Correctness & reliability
- **Idempotency keys.** A client retry on a flaky network currently creates a duplicate booking. I'd accept an `Idempotency-Key` header and de-duplicate retried requests — the most important production gap.
- **Multi-instance safety.** The no-overbooking guarantee relies on `ConcurrentHashMap.compute`, which is correct only within a single JVM. With a database I'd enforce it via optimistic locking (`@Version`) or a conditional update so it holds across instances.
- **Payment lifecycle.** Bookings are confirmed immediately. A real system would hold a seat in `PENDING`, confirm on payment authorization, and auto-release on timeout or payment failure — implying idempotent payments and a compensating action for refunds on cancellation.

### API quality
- **RFC 7807 error responses.** Errors now use a single consistent JSON shape, but I'd adopt `application/problem+json` (RFC 7807) for a standardized, machine-readable error contract.
- **Robust input handling.** Malformed JSON and type-mismatch requests bypass the unified error shape and fall back to Spring defaults; I'd add handlers so every error path is consistent.
- **API versioning and an OpenAPI spec** so the contract is discoverable and can evolve safely.

### Observability & testing
- **Surface the correlation id.** It's set in the MDC and printed via the log pattern, but I'd add structured (JSON) logging and metrics (booking attempts, 409 rate, latency) via Micrometer.
- **Stronger concurrency tests.** The tests share a single `MockMvc` across threads, which isn't formally thread-safe, and assert only counts. I'd harden these and run them as part of CI rather than ad hoc.

### Process
- The Step 1 commit history contains some duplicate prompt-commits and merge commits from the agent's working branch. With more time I'd have kept a cleaner, strictly linear iteration history with one distinct prompt per commit.
