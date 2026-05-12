# jirani-rust

Optional Rust/Rocket gateway for Jirani minimized report sync.

The Android app stays offline-first. This server is only a trusted gateway for communities or OSF-style partners that want anonymized report aggregation, backup of minimized envelopes, and download by other Jirani Android apps.

Companion Android repo:

```text
/home/ewanyonyi/dev/jirani
```

## Development Setup

Install:

- Rust 2021 toolchain.
- Cargo.
- Docker with Compose support, if you want local PostgreSQL.

Check the project:

```bash
cargo fmt -- --check
cargo test
```

### Local In-Memory Mode

This is the fastest development mode. Data is lost when the process exits.

```bash
cargo run
```

By default Rocket listens on `0.0.0.0:8080`, which matches the Android emulator URL:

```text
http://10.0.2.2:8080
```

### Local JSON-File Mode

Use JSON files when you want durable demo storage without running PostgreSQL.

```bash
JIRANI_STORE_PATH=./data/envelopes.json cargo run
```

Relay bundle storage can be enabled separately:

```bash
JIRANI_STORE_PATH=./data/envelopes.json \
JIRANI_RELAY_STORE_PATH=./data/relay-bundles.json \
cargo run
```

### Local PostgreSQL Mode

Start PostgreSQL 16:

```bash
docker compose up -d postgres
```

Then run the gateway with:

```bash
JIRANI_DATABASE_URL=postgres://jirani:jirani_dev_password@localhost:5432/jirani_gateway \
cargo run
```

When `JIRANI_DATABASE_URL` is set, the gateway stores sync envelopes and relay
bundles in PostgreSQL. It creates the demo tables automatically at startup.

## Production Setup

This gateway is still a prototype, but a hosted deployment should use PostgreSQL
instead of in-memory or JSON-file storage.

Required production-style environment:

```bash
JIRANI_DATABASE_URL=postgres://USER:PASSWORD@HOST:5432/jirani_gateway
JIRANI_GATEWAY_TOKEN=use-a-long-random-token
ROCKET_ADDRESS=0.0.0.0
ROCKET_PORT=8080
```

Optional relay encryption key publication:

```bash
JIRANI_RELAY_PUBLIC_KEY=base64-or-pem-public-key
```

Run:

```bash
cargo run --release
```

Before exposing the gateway:

- terminate HTTPS at a reverse proxy or platform load balancer;
- set `JIRANI_GATEWAY_TOKEN` and keep it out of source control;
- use a managed or backed-up PostgreSQL database;
- disable or anonymize reverse-proxy access logs;
- rotate demo/test tokens after presentations;
- keep `/health` public, but protect sync, relay, analytics, and dashboard routes;
- avoid storing raw safety reports, reporter identity, device IDs, exact GPS, or
  exact-home details.

For real community deployment, add community-controlled authentication,
retention/deletion jobs, encrypted storage review, and a local safety expert
review of PII detection before relying on this as production infrastructure.

## Endpoints

- `GET /health`
- `GET /privacy`
- `GET /privacy-page`
- `GET /`
- `GET /reports`
- `GET /analysis`
- `POST /sync/envelopes`
- `GET /sync/envelopes`
- `POST /relay/bundles`
- `GET /relay/bundles`
- `GET /relay/public-key`
- `GET /analytics/anonymous-summary`

## Dashboard And Auth

For local demos, the gateway is open by default. For a hosted test server, set a shared token:

```bash
JIRANI_GATEWAY_TOKEN=change-this-demo-token \
JIRANI_DATABASE_URL=postgres://USER:PASSWORD@HOST:5432/jirani_gateway \
cargo run
```

When `JIRANI_GATEWAY_TOKEN` is set:

- API clients must send `Authorization: Bearer change-this-demo-token`.
- Browser dashboard pages can be opened with `?token=change-this-demo-token`.
- `GET /health` remains public for simple uptime checks.

Dashboard pages:

- `/`: overview cards and recent reports.
- `/reports`: accepted minimized envelope list.
- `/analysis`: anonymous aggregate counts by sensitivity, verification status, and coarse area.
- `/privacy-page`: plain-language privacy posture.

## Anonymity And Reliability

A direct HTTPS request always exposes the connecting IP address at the network layer. This gateway cannot cryptographically hide that from the network path by itself. For stronger IP anonymity from the gateway operator, place a trusted relay/proxy in front of the server or route traffic through infrastructure that strips/anonymizes source logs.

What this gateway does by default:

- does not store IP addresses, User-Agent values, device IDs, precise locations, or reporter identities in application storage;
- persists only minimized accepted envelopes when `JIRANI_STORE_PATH` is set;
- persists accepted relay bundles when `JIRANI_RELAY_STORE_PATH` is set;
- uses PostgreSQL for accepted envelopes and relay bundles when `JIRANI_DATABASE_URL` is set;
- verifies `contentHash` before storage;
- deduplicates without overwriting an existing envelope;
- rejects survivor-centered, expired, PII-looking, or hash-mismatched uploads.

For hosted testing:

- use HTTPS;
- set `JIRANI_GATEWAY_TOKEN`;
- set `JIRANI_DATABASE_URL`;
- disable or anonymize reverse-proxy access logs;
- rotate the test token after demos.

## Android Communication

Android defaults to the emulator URL `http://10.0.2.2:8080`. For a hosted test server, build the Android app with:

```bash
cd /home/ewanyonyi/dev/jirani
./gradlew assembleDebug \
  -PJIRANI_REMOTE_GATEWAY_URL=https://your-test-gateway.example \
  -PJIRANI_REMOTE_GATEWAY_TOKEN=change-this-demo-token
```

See `docs/ANDROID_INTEGRATION.md` for the full API contract shared by both repos.

## Privacy Rules

- Accepts minimized sync envelopes only.
- Verifies `contentHash` against the sanitized payload before storing.
- Rejects survivor-centered GBV/domestic reports in the default gateway flow.
- Rejects obvious phone-number-like values and exact-home hints.
- Deduplicates by `envelopeId`; duplicate uploads return `409 Conflict`, which Android treats as already uploaded.

## Relay Bundles

The relay API is separate from minimized sync envelopes. It is intended for
Android's offline mesh relay flow:

- `POST /relay/bundles`: accept a privacy-safe relay bundle.
- `GET /relay/bundles`: return accepted relay bundles.
- `GET /relay/public-key`: return `JIRANI_RELAY_PUBLIC_KEY` when configured, or
  `404 Not Found` when no relay public key is configured.

Relay bundles contain a minimized public header plus an opaque encrypted payload.
The default gateway validates hashes, expiry, survivor-safety rules, and obvious
PII in the public header. It does not decrypt the private payload.

This scaffold uses in-memory storage unless `JIRANI_DATABASE_URL` is set for
PostgreSQL, or `JIRANI_STORE_PATH` and/or `JIRANI_RELAY_STORE_PATH` are set for
JSON-file demo storage.
