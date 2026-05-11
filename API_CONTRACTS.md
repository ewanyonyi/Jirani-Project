# API Contracts for Jirani App

## Direction
The Android MVP is local-first and does not require a central API. Any backend API is optional and should support gateway sync or anonymized analytics without replacing offline participation.

The full device-to-device and optional OSF-hosted Rust gateway flow is documented in [docs/INFORMATION_FLOW.md](docs/INFORMATION_FLOW.md).

## Optional Rust Gateway Endpoints
- POST /sync/envelopes: Submit minimized sync envelopes for trusted gateway storage, deduplication, and anonymous analysis. Android sends only sanitized report fields plus envelope metadata and content hash.
- GET /sync/envelopes: Fetch minimized envelopes available to trusted Jirani Android apps. Android accepts either a JSON array or `{ "envelopes": [...] }` and verifies `contentHash` before storing.
- GET /analytics/anonymous-summary: Fetch aggregate metrics without PII for trusted demo dashboards.

## Companion Server Repository
The optional Rust/Rocket gateway lives at `/home/ewanyonyi/dev/jirani-rust`.

Use `JIRANI_REMOTE_GATEWAY_URL` as a Gradle property or environment variable to point Android at a hosted test server:

```bash
./gradlew assembleDebug \
  -PJIRANI_REMOTE_GATEWAY_URL=https://your-test-gateway.example \
  -PJIRANI_REMOTE_GATEWAY_TOKEN=change-this-demo-token
```

## Local Data Formats
- Kotlin domain models for agent input/output.
- Room entities for persisted local records.
- SyncEnvelope records for delayed peer-to-peer or gateway exchange.

## Privacy Rules
- Do not require names, phone numbers, or device identity.
- Do not upload raw safety reports without explicit consent.
- Prefer aggregated or encrypted payloads for optional backend prototypes.
- Domestic violence and GBV survivor-centered reports must not be uploaded to the Rust gateway by the default app flow.
- Direct HTTPS does not hide source IP from the server/network layer; the gateway must avoid storing IP, User-Agent, device, precise location, or reporter identity. Use a trusted relay/proxy if source-IP anonymity from the gateway operator is required.
