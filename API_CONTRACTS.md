# API Contracts for Jirani App

## Direction
The Android MVP is local-first and does not require a central API. Any backend API is optional and should support gateway sync or anonymized analytics without replacing offline participation.

The full device-to-device and optional OSF-hosted Rust gateway flow is documented in [docs/INFORMATION_FLOW.md](docs/INFORMATION_FLOW.md).

## Optional Future Endpoints
- POST /sync/envelopes: Submit encrypted or minimized sync envelopes.
- GET /sync/envelopes: Fetch envelopes available for a trusted community gateway.
- POST /analytics/anonymous-summary: Submit opt-in aggregate metrics without PII.

## Local Data Formats
- Kotlin domain models for agent input/output.
- Room entities for persisted local records.
- SyncEnvelope records for delayed peer-to-peer or gateway exchange.

## Privacy Rules
- Do not require names, phone numbers, or device identity.
- Do not upload raw safety reports without explicit consent.
- Prefer aggregated or encrypted payloads for optional backend prototypes.
