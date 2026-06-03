# Freight Nexus

[![CI](https://github.com/SculptingSystems/freight-nexus/actions/workflows/ci.yml/badge.svg)](https://github.com/SculptingSystems/freight-nexus/actions)
![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-yellow)

B2B freight distribution backend connecting carriers with shippers — inspired by the core platform capabilities of HERE Technologies and Samsara. Three-role system: carriers manage fleets and lanes, shippers book loads, drivers post GPS pings and update delivery status.

## What makes this technically interesting

**Partial capacity booking with pessimistic locking.** Unlike hotel rooms (whole unit), trucks carry multiple shipments. The `vehicle_capacity` table tracks booked weight and volume per vehicle per date. When a shipper books a load, `SELECT FOR UPDATE` locks the capacity row — two shippers can't simultaneously consume the last 200 kg. The lock duration is one transaction (validate → deduct → create load).

**HOS compliance engine.** The FMCSA Hours of Service rules are enforced server-side at load assignment time, not as a suggestion. Four rules checked atomically: 11-hour daily driving limit, 14-hour on-duty window, 30-minute break after 8 consecutive hours, and 70-hour rolling 8-day limit. Violating any rule blocks the assignment with a specific error identifying which rule was breached.

**Haversine ETA.** `GET /loads/{id}/tracking/live` returns the driver's current GPS position and a calculated ETA. Distance remaining uses the Haversine formula (great-circle between current coordinates and lane destination). Speed is a 5-event moving average from recent tracking pings — more stable than point-in-time speed which spikes on acceleration/braking.

**Idempotent GPS ingestion.** Driver devices retry on network failure. A unique constraint on `(load_id, device_timestamp)` makes duplicate pings safe — the second insert fails gracefully and returns the existing record. No duplicate events, no error to the driver.

**Immutable status audit trail.** Every load status transition writes a `load_status_history` row with actor type, actor ID, and a note. This table is insert-only. Regulatory and dispute contexts require knowing exactly who moved a load from PICKED_UP to IN_TRANSIT and when — this makes it queryable.

**Transactional Outbox for webhook delivery.** Shipment status change events are written in the same database transaction as the status update. A scheduler delivers with exponential backoff (1 min, 2 min, 4 min... up to 10 attempts). Events survive process crashes; fire-and-forget does not.

## Domain model

```
Partner (CARRIER | SHIPPER)
  │
  ├─ [CARRIER] Vehicle          (type, weight_capacity_kg, volume_capacity_m3)
  │              └─ VehicleCapacity  (available weight/volume per date — lockable)
  │
  ├─ [CARRIER] Driver           (license, HOS state)
  │              └─ HOSWindow   (rolling regulatory state per driver per day)
  │
  ├─ [CARRIER] Lane             (origin → destination with lat/lon for ETA)
  │              └─ RatePlan    (FLAT | PER_KG | PER_KM pricing)
  │
  └─ ShipperContract            (CARRIER grants SHIPPER access to rate plans)

Shipment (cargo: weight_kg, volume_m3, hazmat_flag, declared_value)
  └─ Load                       (PENDING → ASSIGNED → PICKED_UP → IN_TRANSIT → DELIVERED | FAILED)
       ├─ LoadStatusHistory      (immutable audit trail — insert-only)
       └─ TrackingEvent          (GPS pings — append-only, deduplicated by device_timestamp)

WebhookOutbox                   (event delivery queue with exponential-backoff retry)
```

## Three-role JWT

| Role | Who | Authenticates via |
|---|---|---|
| `CARRIER` | Logistics company managing fleet | `POST /auth/login` |
| `SHIPPER` | Company sending cargo | `POST /auth/login` |
| `DRIVER` | Driver posting location updates | `POST /auth/driver-login` |

## Stack

Java 21 · Spring Boot 3.3 · PostgreSQL 16 · Flyway 10 · Spring Security + JWT · Micrometer/Prometheus · Docker (multi-stage build) · GitHub Actions CI

Thirteen Flyway migrations, never `ddl-auto=create`. `@SQLRestriction("deleted_at IS NULL")` on soft-deleted entities. `@EntityGraph` on load list endpoints to prevent N+1. Pessimistic write locks in `VehicleCapacityRepository` and `WebhookOutboxRepository`.

## Quick start

Docker Desktop must be running.

```bash
git clone https://github.com/SculptingSystems/freight-nexus.git
cd freight-nexus
cp .env.example .env
docker compose up
```

First run builds the image (~2 min). Verify:

```bash
curl http://localhost:8080/actuator/health
```

**Explore the API:** open `http://localhost:8080/swagger-ui.html`. Click Authorize, paste the JWT from `POST /auth/login` or `POST /auth/driver-login`, and call any endpoint directly from the browser.

The full carrier-to-driver flow as executable HTTP requests is in [`docs/api-examples.http`](docs/api-examples.http).

## API overview

Public:
```
POST /partners                  Register a carrier or shipper
POST /auth/login                Carrier/shipper login → JWT
POST /auth/driver-login         Driver login → JWT (DRIVER role)
GET  /lanes/search              Search available lanes by origin/destination
GET  /actuator/health
GET  /actuator/prometheus
GET  /swagger-ui.html
```

Carrier (CARRIER role):
```
POST /vehicles
PUT  /vehicles/{id}/capacity    Set available weight/volume for a date
POST /drivers
GET  /drivers/{id}/hos-status   Current HOS state for a driver
POST /lanes
POST /lanes/{id}/rate-plans
POST /contracts
PUT  /contracts/{id}/activate
PUT  /contracts/{id}/terminate
```

Shipper (SHIPPER role):
```
POST /shipments
POST /loads                     Book a load — enforces HOS and capacity
GET  /loads                     My shipment history (paginated)
GET  /loads/{id}/tracking/live  Current position + ETA
GET  /loads/{id}/history        Full status audit trail
```

Driver (DRIVER role):
```
POST /loads/{id}/tracking       Post GPS ping (idempotent, safe to retry)
PUT  /loads/{id}/status         Update load status (PICKED_UP, IN_TRANSIT, DELIVERED)
```

## Running tests

Docker must be running.

```bash
mvn test
```

CI runs the same suite on every push with a GitHub Actions PostgreSQL service container. JaCoCo coverage report is uploaded as a CI artifact on every run.

## Development setup

```bash
docker compose up -d db
mvn spring-boot:run
```

## Deployment

```bash
JWT_SECRET=<your-secret> DB_PASSWORD=<your-password> docker compose up
```

Multi-stage Docker build: Maven compiles in stage 1, JRE-only image in stage 2 (~200 MB). Runs as non-root user.

## Known scope

| Gap | Production approach |
|---|---|
| HOS reset after 34h restart not auto-triggered | Add a scheduled job to reset HOSWindow when 34h off-duty detected from TrackingEvents |
| Webhook retry capped at 10 | Dead-letter queue (SQS, RabbitMQ) |
| No distributed tracing | OpenTelemetry agent |
| No rate limiting on GPS endpoint | API gateway or Resilience4j |
| ETA assumes constant speed | Integrate HERE Routing API for real traffic-aware ETA |

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Security issues: [SECURITY.md](SECURITY.md). Code of conduct: [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

## License

MIT. See [LICENSE](LICENSE).
