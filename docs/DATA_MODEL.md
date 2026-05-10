# Data Model for Jirani App

## Principles
- Store community records locally first.
- Do not require names, phone numbers, government IDs, or personal profiles.
- Use anonymous labels such as Party A, Party B, Reporter, or Trusted Verifier.
- Keep sync metadata separate from record content.
- Encrypt sensitive local records before production release.

## MVP Entities
- DisputeRecord: id, description, concerns, status, createdAt, updatedAt
- MediationGuidanceRecord: id, disputeId, summary, recommendations, safetyNote, createdAt
- AgreementRecord: id, disputeId, neutralSummary, commitments, reviewDate, createdAt
- SafetyReportRecord: id, reportType, generalLocation, timeWindow, incidentSummary, sensitivity, verificationStatus, expiresAt, createdAt
- SanitizedReportPayload: reportType, generalArea, timeWindow, observedRisk, verificationStatus, sensitivity
- SyncEnvelope: id, recordType, recordId, contentHash, version, lastModifiedAt, audienceTier, syncState, allowedTransports

All `id` values should be cryptographically random non-PII identifiers. They must not be derived from phone numbers, device IDs, network addresses, GPS, user names, or exact timestamps.

## Relationships
- DisputeRecord can have many MediationGuidanceRecords.
- DisputeRecord can have zero or more AgreementRecords.
- SafetyReportRecord is independent from disputes unless a community explicitly links it locally.
- SyncEnvelope tracks local-first sharing without storing personal identity.
- Domestic violence and GBV records use `survivor_centered` sensitivity and must not be linked to a mediation case by default.

## Storage Direction
- Local MVP: Room database backed by SQLite.
- Sensitive fields: encrypted storage pattern before real-world use.
- Sync: delayed nearby exchange with conflict-aware envelopes and sensitivity-based transport rules.
- Cloud/backend: optional prototype extension, not required for core participation.
- Information flow: see `INFORMATION_FLOW.md` for BLE/Nearby/Wi-Fi Direct enabled and disabled sharing paths through an optional trusted Rust analytics gateway.
