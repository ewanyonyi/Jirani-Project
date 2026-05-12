# Jirani Communication & Anonymous Relay Specification

This document describes the proposed Phase 2 offline-first relay layer for
Jirani. The current MVP already supports minimized `SyncEnvelope` upload and
download through the optional Rust/Rocket gateway. Relay bundles are a separate
contract so the existing Android gateway sync can stay stable.

The goal is to support community safety alerts and delayed synchronization while
preserving Jirani's core privacy posture: the gateway must not require or store
reporter names, phone numbers, device IDs, exact GPS coordinates, exact homes, or
raw survivor-centered reports.

## 1. Current MVP vs Phase 2 Relay

| Layer | Current Android behavior | Phase 2 relay direction |
| :--- | :--- | :--- |
| Local state | Kotlin Coroutines/Flow with SharedPreferences-backed demo state. | Room database for relay bundles waiting for local relay or remote sync. |
| Nearby transport | Nearby Connections sends encrypted `WireReportPacket` values derived from `SyncEnvelope`. | Nearby Connections carries `RelayBundle` values with public headers and encrypted payloads. |
| Remote gateway | `POST /sync/envelopes` and `GET /sync/envelopes` for minimized reports. | Add `/relay/bundles` without changing `/sync/envelopes`. |
| Background relay | Runtime starts advertising/discovery when permissions and settings allow it. | Foreground service only when a user explicitly opts into active relay mode. |
| Verification signal | Delivered-device counts and local content-hash checks. | Multi-peer relay trust signal based on repeated receipt of the same `bundleHash`. |

## 2. Architecture: Aware Relay DTN

Jirani can use an "Aware Relay" delay-tolerant networking model. Android devices
may carry relay bundles between neighbors when internet access is unavailable.
Each relay bundle has two parts:

1. **Public header:** A minimized, cleartext safety summary that lets the carrier
   understand immediate local risk. This can include coarse area, alert type,
   risk level, time bucket, and verification status.
2. **Private encrypted payload:** An opaque encrypted blob that relay devices and
   the gateway do not inspect by default. This should not be treated as a license
   to collect PII. Any future decryption flow needs a separate threat model,
   retention policy, and community-controlled authorization.

The public header and encrypted payload are hashed together. If either part is
modified, the bundle hash changes and the receiving app or gateway rejects it.

## 3. Android Responsibilities

The Android app owns the offline mesh, carrier experience, and local relay
decision-making.

| Responsibility | Android implementation |
| :--- | :--- |
| Mesh transport | Google Nearby Connections API, using a peer-to-peer cluster strategy. |
| Local state | Kotlin Coroutines and Flow for reactive relay state. |
| Data persistence | Room database for bundles waiting for local relay or remote sync in Phase 2. |
| UI rendering | Jetpack Compose, observing Flow state. |
| Background sync | Foreground service when the user opts into active relay mode in Phase 2. |

Android should implement:

- generation of minimized public headers from local reports;
- encryption of any private payload before storage or relay;
- local validation of bundle hashes before import;
- Nearby handoff with `ConnectionsClient.sendPayload()`;
- local deduplication by `bundleId` or `bundleHash`;
- multi-peer verification, where a "High Alert" UI state requires the same
  bundle hash from at least two different neighbor sessions;
- rate limiting for gossip events to reduce battery drain and spam;
- a user-visible relay toggle, enabled by default only when appropriate for the
  community demo and platform permissions.

The Android app now includes relay-bundle domain models and deterministic hash
helpers for the proposed contract. Transport wiring, Room persistence,
foreground service behavior, and public-header inbox UI remain future work.

### Android UI Concepts

The Android app may expose a "Relay Shield" area in the Compose dashboard:

- current mesh status, such as "Mesh active";
- active public-header alerts, such as "Neighbor alert near shared grazing area";
- count of anonymous packets carried;
- a share/relay toggle;
- a subtle mesh range or sync activity indicator.

The UI should avoid implying that unverified reports are confirmed incidents.
Alert copy should use cautious language such as "reported", "pending review",
or "needs local verification".

## 4. Rust/Rocket Gateway Responsibilities

The companion repository at `/home/ewanyonyi/dev/jirani-rust` owns the optional
internet gateway. It should stay small, auditable, and compatible with the
existing minimized sync API.

The current gateway contract supports:

- `POST /sync/envelopes` for minimized report sync envelopes;
- `GET /sync/envelopes` for trusted Android downloads;
- `GET /analytics/anonymous-summary` for aggregate non-PII counts;
- token auth for hosted demos;
- content-hash validation, deduplication, expiry checks, and basic PII rejection.

Storage is selected by environment:

- default local runs use in-memory storage;
- `JIRANI_STORE_PATH` and `JIRANI_RELAY_STORE_PATH` enable JSON-file demo
  persistence;
- `JIRANI_DATABASE_URL` enables PostgreSQL storage for accepted sync envelopes
  and relay bundles.

### Current Sync Envelope Flow

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

Nearby relay and gateway upload are independent. A report can reach five nearby
devices and still upload later when the app reaches the Rust gateway.

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

### Gateway Configuration

Android emulator default:

```text
http://10.0.2.2:8080
```

Rust server:

```bash
cd /home/ewanyonyi/dev/jirani-rust
cargo run
```

For a hosted test server, build Android with `JIRANI_REMOTE_GATEWAY_URL` set to
the hosted base URL.

Gradle property:

```bash
./gradlew assembleDebug \
  -PJIRANI_REMOTE_GATEWAY_URL=https://your-test-gateway.example \
  -PJIRANI_REMOTE_GATEWAY_TOKEN=change-this-demo-token
```

Environment variable:

```bash
JIRANI_REMOTE_GATEWAY_URL=https://your-test-gateway.example \
JIRANI_REMOTE_GATEWAY_TOKEN=change-this-demo-token \
./gradlew assembleDebug
```

If the Rust server does not set `JIRANI_GATEWAY_TOKEN`, omit
`JIRANI_REMOTE_GATEWAY_TOKEN` and Android will not send an auth header.

Use HTTPS for hosted testing. The Android manifest permits cleartext only for
emulator/local development hosts through `network_security_config.xml`, and
Android rejects non-HTTPS gateway URLs outside local development hosts
(`10.0.2.2`, `localhost`, `127.0.0.1`).

Relay support should be integrated as a separate API surface instead of changing
the existing sync envelope contract:

- `POST /relay/bundles`: accept privacy-safe relay bundles from trusted Android
  clients.
- `GET /relay/bundles`: return accepted relay bundles for trusted Android
  clients.
- `GET /relay/public-key`: optional endpoint for Android to fetch the gateway's
  configured encryption public key.

The Rust gateway should validate and store only:

- a bundle ID;
- a minimized public header;
- an encrypted payload as an opaque string or byte encoding;
- payload and bundle hashes;
- coarse timestamps or buckets;
- expiry metadata.

The gateway should not decrypt private payloads in the default demo flow. It
should reject relay bundles when:

- the bundle is expired;
- the public header contains obvious PII or exact-home hints;
- the public header is survivor-centered or marked survivor-support-only;
- the encrypted payload is missing when required;
- the payload hash or full bundle hash does not match;
- the bundle was already stored.

## 5. Proposed Relay Bundle Shape

This shape is intentionally separate from `SyncEnvelope` so the existing Android
gateway contract can remain stable.

```json
{
  "bundleId": "bundle-demo-001",
  "publicHeader": {
    "alertType": "ResourceDispute",
    "generalArea": "near river",
    "timeWindow": "morning",
    "riskLevel": "Elevated",
    "message": "Cattle movement reported near shared grazing boundary.",
    "verificationStatus": "PendingVerification",
    "audienceTier": "TrustedVerifier",
    "sensitivity": "Community"
  },
  "encryptedPayload": "base64-encoded-ciphertext",
  "payloadHash": "hex-sha256-of-encrypted-payload",
  "bundleHash": "hex-sha256-of-public-header-and-payload-hash",
  "expiresAtEpochSeconds": 1900000000
}
```

Hashing must use a deterministic representation shared by Android and Rust. The
Android model lives in `RelayBundleModels.kt`. If relay endpoints are
implemented, update this document,
`/home/ewanyonyi/dev/jirani-rust/docs/ANDROID_INTEGRATION.md`, and Rust
integration tests together.

Android computes `payloadHash` as the SHA-256 hex digest of the
`encryptedPayload` string bytes. Android computes `bundleHash` as the SHA-256
hex digest of the public-header fields joined in this order, followed by
`payloadHash`. Rust must use the same contract:

```text
alertType|generalArea|timeWindow|riskLevel|message|verificationStatus|audienceTier|sensitivity|payloadHash
```

## 6. Security And Trust Guardrails

- A relay carrier may know the coarse public safety warning, but never who
  reported it.
- The public header must remain minimized and should not include names, phone
  numbers, exact homes, exact GPS coordinates, or household identifiers.
- The encrypted payload is opaque to this gateway by default.
- The current Android relay encryption helper uses a demo key for local packet
  transfer. Production relay bundles need community-controlled key management or
  gateway public-key retrieval before private payloads are used.
- Direct HTTPS upload still exposes source IP at the network layer. Hosted
  deployments should use HTTPS, token auth, durable storage, and anonymized
  reverse-proxy logs.
- Android sends only minimized envelopes and stable, low-fingerprint sync
  headers through the current gateway flow. Android sends no device ID, reporter
  ID, GPS, phone number, account identity, or precise location.
- The Rust gateway should not store IP, User-Agent, device identity, or precise
  location. When `JIRANI_STORE_PATH` is configured, it should persist only
  accepted minimized envelopes.
- For stronger IP anonymity from the gateway operator, place a trusted
  relay/proxy in front of the Rust server and disable or anonymize proxy access
  logs.
- Multi-peer verification is an Android-side UI trust signal, not proof that an
  incident occurred.
- Survivor-centered GBV/domestic reports must remain outside broad relay,
  analytics, and default gateway sync.
- Unverified anonymous reports are not confirmed facts.

Every neighbor can be a relay, but every relay must remain privacy-preserving.
