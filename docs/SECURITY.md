# Security Guidelines for Jirani App

## Data Protection
- Community records are stored locally first.
- Sensitive local records should be encrypted before production use.
- No sensitive information should be transmitted without explicit community/user consent.
- Sync metadata should be minimal and separated from record content where possible.
- Follow Android security best practices for storage, backup, and export controls.

## Authentication
- The MVP should not require account registration, phone numbers, government IDs, or named profiles.
- Anonymous participation is the default.
- Recovery phrases may be explored later for local restoration without requiring centralized identity.

## Privacy
- Avoid collecting PII in mediation, agreement, and reporting flows.
- Use neutral labels such as Party A, Party B, Reporter, and Trusted Verifier.
- Ask only for non-identifying incident details needed for safety: general location, time window, and observed risk.
- Encourage trusted local verification before wider sharing of safety reports.
- Offline-first behavior reduces unnecessary data transmission.
