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
- SafetyReportRecord: id, threatType, generalLocation, timeWindow, incidentSummary, verificationStatus, createdAt
- SyncEnvelope: id, recordType, recordId, version, lastModifiedAt, syncState

## Relationships
- DisputeRecord can have many MediationGuidanceRecords.
- DisputeRecord can have zero or more AgreementRecords.
- SafetyReportRecord is independent from disputes unless a community explicitly links it locally.
- SyncEnvelope tracks local-first sharing without storing personal identity.

## Storage Direction
- Local MVP: Room database backed by SQLite.
- Sensitive fields: encrypted storage pattern before real-world use.
- Sync: delayed peer-to-peer exchange with conflict-aware envelopes.
- Cloud/backend: optional prototype extension, not required for core participation.
