# Remote Rust Gateway Integration

## Companion Repository

The optional Rust/Rocket gateway lives in:

```text
/home/ewanyonyi/dev/jirani-rust
```

The Android app remains offline-first and peer-to-peer first. The Rust server is an optional test or partner gateway for minimized report sync, anonymous analysis, and download by other Jirani Android apps.

## Communication Flow

Android sends only sanitized sync envelopes to the gateway:

```text
Jirani Android app
  -> POST /sync/envelopes
  -> Rust/Rocket gateway
  -> stores minimized envelope if privacy and hash checks pass

Jirani Android app
  -> GET /sync/envelopes
  -> verifies contentHash locally
  -> stores valid items in the receiving-device inbox
```

Nearby relay and gateway upload are independent. A report can reach five nearby devices and still upload later when the app reaches the Rust gateway.

## Default Local Development

Android emulator:

```text
http://10.0.2.2:8080
```

Rust server:

```bash
cd /home/ewanyonyi/dev/jirani-rust
cargo run
```

## Hosted Test Server

For a remote test server, build Android with `JIRANI_REMOTE_GATEWAY_URL` set to the hosted base URL.

Gradle property:

```bash
./gradlew assembleDebug -PJIRANI_REMOTE_GATEWAY_URL=https://your-test-gateway.example
```

Environment variable:

```bash
JIRANI_REMOTE_GATEWAY_URL=https://your-test-gateway.example ./gradlew assembleDebug
```

Use HTTPS for hosted testing. The Android manifest permits cleartext only for emulator/local development hosts through `network_security_config.xml`.

## Endpoint Contract

The Android app expects:

- `POST /sync/envelopes`
  - returns `2xx` for stored uploads;
  - returns `409 Conflict` for duplicates already stored;
  - returns `400 Bad Request` for integrity, expiry, or privacy rejection.
- `GET /sync/envelopes`
  - returns either a JSON array or `{ "envelopes": [...] }`;
  - each item must include `contentHash` and `payload`;
  - Android verifies `contentHash` before importing.
- `GET /analytics/anonymous-summary`
  - optional aggregate endpoint for trusted demo dashboards.

## Shared Privacy Rules

Both repos must preserve the same constraints:

- no raw reports in gateway payloads;
- no names, phone numbers, exact homes, device IDs, or GPS coordinates;
- no survivor-centered GBV/domestic reports through default gateway sync;
- content hash verification before storage or import;
- unverified anonymous reports are not confirmed facts.
