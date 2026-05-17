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
