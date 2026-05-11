# AGENTS.md

## Project Identity

Jirani Rust is the optional Rust/Rocket gateway for the Jirani PeaceTech Android app.

The Android app remains offline-first. This server supports communities, trusted local actors, and OSF-style partners only when internet access is available and a community chooses to use a gateway for minimized report sync, anonymous analysis, or cross-device download.

Companion Android repository:

```text
/home/ewanyonyi/dev/jirani
```

## Core Values

- Prioritize community safety over feature speed.
- Preserve reporter anonymity by default.
- Support local verification before escalation.
- Treat anonymous reports as signals for review, not confirmed facts.
- Keep survivor-centered GBV/domestic reports out of broad sync and analytics.
- Avoid centralizing power: the server is optional, not required for participation.
- Build for low bandwidth, unstable connectivity, and practical field demos.

## Setup

Use Rust 2021 edition and Cargo.

Common commands:

- Format: `cargo fmt`
- Check formatting: `cargo fmt -- --check`
- Run tests: `cargo test`
- Run the Rocket server: `cargo run`

The default Rocket port is `8080`, matching the Android emulator gateway URL:

```text
http://10.0.2.2:8080
```

## Architecture

Use a small, readable Rocket service:

- `src/main.rs`: binary entrypoint.
- `src/lib.rs`: testable Rocket builder.
- `src/routes.rs`: HTTP route handlers.
- `src/models.rs`: request/response structs and validation.
- `src/store.rs`: storage abstraction.
- `tests/`: integration tests for API behavior and privacy safeguards.

Start with in-memory storage for demo work. Add persistent storage only when required, and keep the storage boundary clear enough to replace with SQLite/Postgres later.

## API Contract

The server should match the Android gateway contract:

- `POST /sync/envelopes`: accept minimized sync envelopes.
- `GET /sync/envelopes`: return minimized envelopes for trusted Jirani Android apps.
- `GET /analytics/anonymous-summary`: return aggregate, non-PII counts.
- `GET /health`: simple service health check.
- `GET /privacy`: machine-readable gateway privacy posture.
- `GET /`, `GET /reports`, and `GET /analysis`: simple protected dashboard pages for demo review.

When changing API fields, update Android, tests, `README.md`, and `docs/ANDROID_INTEGRATION.md` together. Also check `/home/ewanyonyi/dev/jirani/docs/REMOTE_RUST_GATEWAY.md`.

## Authentication

Local development may run without auth. Hosted test servers should set `JIRANI_GATEWAY_TOKEN`.

When token auth is enabled:

- API clients use `Authorization: Bearer <token>`.
- Android should be built with `JIRANI_REMOTE_GATEWAY_TOKEN`.
- Browser dashboard testing may use `?token=<token>`.
- `GET /health` remains public.

This shared token approach is for hackathon/test deployment only. It is not production-grade identity, authorization, or audit control.

## Anonymous Remote Communication

Be precise about anonymity. A direct Android-to-server HTTPS request still exposes source IP at the network layer. The gateway must not store IP, User-Agent, device ID, exact location, or reporter identity in application data.

For hosted testing:

- require HTTPS for non-local Android gateway URLs;
- set `JIRANI_GATEWAY_TOKEN`;
- set `JIRANI_STORE_PATH` for durable envelope storage;
- disable or anonymize reverse-proxy access logs;
- prefer a trusted relay/proxy if source-IP anonymity from the gateway operator is required;
- keep persisted records limited to minimized accepted envelopes and aggregate summaries.

## Rust Style

- Use idiomatic Rust 2021.
- Prefer explicit domain types over loosely typed maps.
- Keep route handlers thin; put validation on models and storage logic in `store`.
- Return clear HTTP statuses:
  - `201 Created` for new envelope uploads.
  - `409 Conflict` for duplicate uploads already stored.
  - `400 Bad Request` for privacy, expiry, or integrity failures.
- Avoid panics in request paths. A poisoned in-memory lock may use `expect`, but production storage should return errors.
- Keep dependencies modest and well justified.
- Prefer deterministic, focused tests for privacy and sync behavior.

## Privacy And Safety Constraints

Never require or store:

- phone numbers,
- emails,
- government IDs,
- device IDs,
- exact GPS coordinates,
- reporter names,
- exact homes, rooms, plots, or household identifiers.

The server must reject default gateway uploads when:

- `contentHash` does not match the sanitized payload,
- the envelope is expired,
- the payload is survivor-centered,
- the audience tier is survivor-support-only,
- obvious phone-number-like or exact-home hints appear.

Do not add routes that expose raw safety reports publicly. Downloads must return only minimized envelopes already accepted by the gateway.

## OSF / Hackathon Requirements

Align implementation with PeaceTech, OSF-style partner, and hackathon expectations:

- Demonstrate impact with a working, understandable API.
- Keep the prototype auditable: clear models, clear validation, clear tests.
- Show low-bandwidth resilience: compact JSON and no heavy real-time dependency.
- Make privacy safeguards visible in code and tests.
- Support anonymous aggregation instead of surveillance.
- Treat verification status as important context in analytics.
- Do not represent unverified reports as confirmed incidents.
- Keep the gateway optional so Jirani still works offline and peer-to-peer.

## Testing

Add or update tests for every behavior change that affects:

- sync envelope upload/download,
- deduplication,
- content hash validation,
- survivor-centered report rejection,
- PII/privacy filters,
- expiry handling,
- anonymous analytics.

Run `cargo fmt -- --check` and `cargo test` before finalizing changes. If tests cannot run because dependencies or network are unavailable, report that clearly.

## Documentation

Update `README.md` when changing:

- endpoints,
- request/response formats,
- run commands,
- privacy behavior,
- Android integration assumptions.

Keep docs honest about demo limitations. If storage is in-memory, say so. If TLS/auth are not yet implemented, say so and do not imply production readiness.

## Security Notes

This scaffold is not production-hardened yet.

Before real deployment:

- use HTTPS only,
- add community-controlled authentication or gateway authorization,
- add persistent encrypted storage where appropriate,
- add retention/deletion jobs,
- add structured audit logs without reporter identity,
- review PII detection with local safety experts,
- threat-model partner/operator access to gateway data.

## Agent Workflow

Follow a write -> test -> review loop:

1. Make a small, focused change.
2. Format and test it.
3. Review the diff for privacy, clarity, and maintainability.
4. Update docs if behavior changed.

Prefer preserving useful guidance and refining it rather than deleting it. Keep feedback constructive and grounded in the PeaceTech context.
