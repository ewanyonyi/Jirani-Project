# Information Flow: Device Sync to Optional OSF Gateway

## Purpose
This document describes how Jirani moves community information from one device to another, then optionally to a Rust server hosted by a trusted partner such as OSF for anonymous analysis and aggregation.

The core rule is that Jirani works locally first. Sharing is delayed, consent-based, and transport-agnostic.

## Data Safety Rules
- Raw safety reports stay local until the user or trusted verifier allows sharing.
- Shared reports use sanitized fields: threat type, general area, approximate time window, observed risk, verification status, and expiry.
- Names, phone numbers, device identifiers, exact GPS, exact homes, and reporter identity are not included in sync payloads.
- Each record and sync envelope uses a random non-PII identifier.
- Sync metadata is stored separately from record content.
- Optional server upload uses minimized or aggregated payloads unless a community explicitly authorizes encrypted envelope handoff.
- Domestic violence and GBV reports are survivor-centered records. They are not community alerts, are not mediation cases, and are not broadly gossiped across nearby devices.

## Core Objects

```text
SafetyReportRecord
  - random recordId
  - threatType
  - generalLocation
  - timeWindow
  - incidentSummary
  - sensitivity: community, protection, or survivor_centered
  - verificationStatus
  - expiresAt

SyncEnvelope
  - random envelopeId
  - recordType
  - recordId
  - contentHash
  - version
  - lastModifiedAt bucket
  - audienceTier
  - syncState
  - allowedTransports
```

## Report Sensitivity Classes

| Class | Examples | Movement Rule |
|---|---|---|
| `community` | grazing dispute, water access, rumor after verification | Can move to trusted verifiers through Nearby Connections, Wi-Fi Direct, Android Sharesheet, QR, or encrypted file. |
| `protection` | attack, armed group, retaliation, road danger | Moves only to trusted protection actors such as elders, chiefs, peace committees, or approved OSF/community contacts. |
| `survivor_centered` | domestic violence, GBV, sexual violence, defilement | Held locally by default. Shared only with a survivor-chosen support actor through private handoff such as encrypted file, QR, or Android Sharesheet. No community alert. No mediation. |

## Transport Choices

- **Nearby Connections:** used when a trusted verifier device is nearby and both people agree to exchange sanitized envelopes.
- **Wi-Fi Direct:** used for larger local handoff where devices can connect directly without internet.
- **Android Sharesheet:** used for explicit, user-driven sharing to a trusted app or contact.
- **QR or encrypted file handoff:** used for the smallest and most sensitive payloads, especially survivor-centered reports.

## Flow A: BLE, Nearby Share, or Wi-Fi Direct Enabled

```text
Device A
  |
  | 1. User creates report/agreement locally
  v
Encrypted local Room/SQLite storage
  |
  | 2. App creates random recordId and SyncEnvelope
  v
Local sync queue
  |
  | 3. Privacy filter removes PII and exact location
  v
Sanitized sync payload
  |
  | 4. Available transport selected by sensitivity
  |    - Nearby Connections for trusted verifier exchange
  |    - Wi-Fi Direct for local direct transfer
  |    - Android Sharesheet for explicit user handoff
  |    - QR/encrypted file for sensitive private handoff
  v
Device B
  |
  | 5. Device B verifies envelope hash and duplicate status
  v
Device B local storage
  |
  | 6. Trusted verifier reviews report if needed
  v
Verified or rejected local status
  |
  | 7. Device B relays only eligible envelopes onward
  v
Other trusted devices / community gateway
  |
  | 8. Gateway uploads opt-in minimized data when internet exists
  v
Optional OSF-hosted Rust server
  |
  | 9. Server stores aggregates, trends, and non-PII analytics
  v
Community dashboards / anonymized insight exports
```

## Flow B: BLE, Nearby Share, and Wi-Fi Direct Disabled

```text
Device A
  |
  | 1. User creates report/agreement locally
  v
Encrypted local Room/SQLite storage
  |
  | 2. App creates random recordId and SyncEnvelope
  v
Pending local sync queue
  |
  | 3. App marks syncState as waiting_for_transport
  v
Local-only record remains usable offline
  |
  | 4. User later enables one sharing path
  |    - BLE / Nearby Connections
  |    - Wi-Fi Direct
  |    - Android Sharesheet export
  |    - QR or encrypted file handoff for small payloads
  v
Sanitized sync payload
  |
  | 5. Payload moves to another trusted device or gateway
  v
Trusted verifier / community gateway
  |
  | 6. Gateway uploads opt-in minimized data when internet exists
  v
Optional OSF-hosted Rust server
  |
  | 7. Server performs aggregation without reporter identity
  v
Anonymous regional safety trends and coordination insights
```

## Optional OSF Rust Server Role
The Rust server is not required for participation. It is a trusted optional gateway for communities that want aggregation, backup of minimized sync envelopes, or regional analysis.

Recommended responsibilities:
- Accept minimized or encrypted sync envelopes.
- Reject payloads containing obvious PII patterns where possible.
- Deduplicate by random recordId and contentHash, not device identity.
- Aggregate by coarse area and time window.
- Track verification status counts instead of raw reporter details.
- Enforce retention limits for sensitive report classes.
- Return only aggregated insights to dashboards or partner exports.

The server should not:
- Require phone numbers, emails, device IDs, or government IDs.
- Store exact GPS for safety reports.
- Publish raw reports as public records.
- Treat unverified anonymous reports as confirmed facts.

## Verification States

```text
local_only
  -> pending_verification
  -> verifier_confirmed
  -> community_alert_ready
  -> aggregated_upload_ready

pending_verification
  -> rejected_or_expired
```

Only `community_alert_ready` and `aggregated_upload_ready` records should be eligible for broad sharing or optional server analytics. Domestic violence and GBV records should not enter broad sharing; they remain survivor-support records unless a trained support process changes their status with explicit consent and legal/child-protection safeguards.

## Integrity Without Public Blockchain
Jirani can use blockchain-inspired integrity without exposing sensitive reports on a public chain:

- Hash each sanitized payload.
- Store an append-only local event log.
- Include the previous event hash when updating a record.
- Allow trusted verifiers to sign approval events.
- Send hashes and signatures inside `SyncEnvelope`.

This gives tamper evidence while preserving offline operation and reporter anonymity.
