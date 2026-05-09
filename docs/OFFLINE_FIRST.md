# Offline-First Architecture for Jirani App

## Overview
The app is designed to work without internet access for core mediation, agreement, and anonymous reporting flows. Synchronization is delayed and opportunistic rather than required for participation.

## Key Components
- Local Room/SQLite database for disputes, guidance, agreements, reports, and sync envelopes.
- Encrypted storage patterns for sensitive fields.
- Local agent logic for mediation, reporting, summary, and translation fallbacks.
- Sync manager for delayed data reconciliation.
- Conflict-aware record versions for repeated peer exchanges.
- Placeholder support for BLE, Wi-Fi Direct, Nearby Connections, or user-driven Android sharing.

## Benefits
- Works in low-connectivity and disrupted environments.
- Keeps core community records under local control.
- Reduces dependency on central servers.
- Supports safer anonymous participation.

## MVP Sync Direction
- Start with local-only records and simulated sync status.
- Add sync envelopes before adding transport-specific code.
- Keep transport adapters replaceable so BLE/Wi-Fi Direct/Ghost-Sync concepts can evolve without changing domain records.
