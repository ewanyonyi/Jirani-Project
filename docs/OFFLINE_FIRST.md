# Offline-First Architecture for Jirani App

## Overview
The app is designed to work without internet access for core reporting, local review, mediation, agreement, and anonymous safety flows. Synchronization is delayed and opportunistic rather than required for participation.

## Key Components
- Local Room/SQLite database for disputes, guidance, agreements, reports, and sync envelopes.
- Encrypted storage patterns for sensitive fields.
- Local agent logic for reporting, mediation readiness, summary, and translation fallbacks.
- Sync manager for delayed data reconciliation.
- Conflict-aware record versions for repeated peer exchanges.
- Placeholder support for BLE, Wi-Fi Direct, Nearby Connections, or user-driven Android sharing.
- Transport-agnostic information flow from device sync queues to an optional trusted Rust analytics gateway.

## Benefits
- Works in low-connectivity and disrupted environments.
- Keeps core community records under local control.
- Reduces dependency on central servers.
- Supports safer anonymous participation.

## MVP Sync Direction
- Start with local-only records and simulated sync status.
- Add sync envelopes before adding transport-specific code.
- Keep transport adapters replaceable so nearby sharing options can evolve without changing domain records.
- Use the device-to-device and optional gateway flow in [INFORMATION_FLOW.md](INFORMATION_FLOW.md) for report sharing and aggregation design.
