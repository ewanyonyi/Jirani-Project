# MVP Scope for Jirani App

## Overview
Jirani is an offline-first PeaceTech Android app for conflict reporting, local verification, elder-led mediation, agreement records, and low-connectivity coordination.

## Core Features
- Anonymous conflict, threat, domestic violence, and GBV reporting with no mandatory personal identifiers.
- Local triage that separates protection cases, rumors, and mediation-ready resource disputes.
- Survivor-centered handling that keeps domestic violence and GBV reports out of community alerts and mediation flows.
- Mediation readiness guidance for elders, peace committees, chiefs, religious leaders, and OSF/community partners.
- Neutral agreement summaries using labels such as Party A and Party B.
- Local-first records designed for later Room/SQLite persistence.
- Offline sync placeholders for delayed peer-to-peer sharing.
- English-first UI with Swahili translation support planned into the architecture.

## MVP Deliverables
- Native Android app using Kotlin and Jetpack Compose.
- Agent-first domain package for mediation, reporting, summary, and translation logic.
- Report-first screen for entering a conflict or threat report and receiving real-world triage.
- Mediation review screen for cases that trusted local actors decide are safe to convene.
- Targeted unit tests for privacy-sensitive domain logic.
- Documentation aligned with no-PII, offline-first architecture.

## Out of Scope for MVP
- Mandatory account registration or user profiles.
- Central-server-only workflows.
- Production-grade BLE/Wi-Fi Direct sync.
- Production-grade on-device LLM inference.
- Advanced analytics and external integrations.
- Direct user-started mediation during active violence.
