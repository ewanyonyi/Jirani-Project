# MVP Scope for Jirani App

## Overview
Jirani is an offline-first PeaceTech Android app for community mediation, anonymous safety reporting, agreement records, and low-connectivity coordination.

## Core Features
- AI-assisted mediation guidance using local rule-based agent logic.
- Anonymous dispute descriptions with no mandatory personal identifiers.
- Neutral agreement summaries using labels such as Party A and Party B.
- Anonymous incident reporting with trusted verification guidance.
- Local-first records designed for later Room/SQLite persistence.
- Offline sync placeholders for delayed peer-to-peer sharing.
- English-first UI with Swahili translation support planned into the architecture.

## MVP Deliverables
- Native Android app using Kotlin and Jetpack Compose.
- Agent-first domain package for mediation, reporting, summary, and translation logic.
- Mediation assistant screen for entering a dispute and generating calm guidance.
- Targeted unit tests for privacy-sensitive domain logic.
- Documentation aligned with no-PII, offline-first architecture.

## Out of Scope for MVP
- Mandatory account registration or user profiles.
- Central-server-only workflows.
- Production-grade BLE/Wi-Fi Direct sync.
- Production-grade on-device LLM inference.
- Advanced analytics and external integrations.
