# CAPSTONE PROJECT: PeaceTech Initiative

# Project Identity

## **Jirani**

> *“An offline-first peace coordination platform that helps communities communicate, mediate disputes, and preserve trusted agreements during conflict and displacement.”*

Derived from the Swahili word **“Jirani” (neighbor)**, the name reflects the belief that peace is built and sustained at the community level through dialogue, trust, and cooperation.

---

# 1. Vision & Problem Statement

Jirani is an offline-first, AI-assisted PeaceTech platform designed to help communities prevent conflict escalation, coordinate peacefully during instability, and recover collaboratively after disputes.

This project is designed to meet the Open Society Foundations PeaceTech challenge by supporting displaced, insecure, and multilingual communities with people-centered, low-connectivity tools that promote accountability, safety, and local agency.

This project is built with Codex integration as a core workflow principle, using prompts, agents, and documented planning artifacts to make the design and implementation process transparent and repeatable.

Using cross-community tensions and resource-sharing disputes such as those experienced in pastoralist and border communities as a guiding case study, Jirani addresses the breakdown of centralized communication and mediation systems by empowering local leaders, elders, and residents with secure, resilient, low-bandwidth tools.

The platform is designed for environments where:

- internet access is unreliable,
- institutions may be inaccessible,
- trust is fragile,
- communities are displaced or recovering from conflict,
- and people need practical tools to communicate and preserve agreements safely.

---

# 2. Core PeaceTech Objectives

Jirani focuses on three key goals:

## A. Conflict Prevention

Help communities de-escalate disputes before they become violent.

## B. Community Mediation

Support local leaders and residents in documenting peaceful resolutions, community decisions, and resource-sharing agreements.

## C. Recovery & Coordination

Enable trusted communication and synchronization of critical community information during displacement, instability, or recovery.

---

# 3. Operating Constraints & Design Response

| Constraint | Jirani Response |
|---|---|
| **Displaced Populations** | Mobile-first experience optimized for unstable environments and low-spec Android devices. |
| **Low Bandwidth** | Offline-first architecture with delayed synchronization when connectivity becomes available. |
| **Secure Access / Anonymity** | No mandatory personal identifiers. Users may operate anonymously using recovery phrases instead of phone numbers or emails. |
| **Decentralized Resilience** | Designed for peer-to-peer device sync, delayed gateway handoff, and minimal central server dependence to preserve a structural digital moat. |
| **Multilingual Communities** | Initial support for English and Swahili, with architecture designed for Somali, Arabic, French, Portuguese, and local languages. |
| **Adaptability Across Regions** | Context-agnostic modular design allowing communities to customize mediation flows, resource categories, and local governance structures. |

---

# 4. Core Features

## A. AI Mediation Assistant

An AI-powered assistant that helps users:

- de-escalate emotionally charged situations,
- communicate respectfully,
- summarize disputes neutrally,
- identify possible common ground,
- and propose practical next steps.

### Example

A user types:

> “Our neighbors blocked access to the water point.”

Jirani responds by:

- suggesting calm communication approaches,
- proposing mediation steps,
- identifying what facts should be clarified,
- and generating neutral discussion prompts.

---

## B. Community Agreement Records

Communities can create and preserve:

- peace agreements,
- grazing schedules,
- water-sharing arrangements,
- local mediation outcomes,
- community recovery plans,
- and aid/resource coordination notes.

Jirani also supports anonymous reporting of threats, such as rustling, robbery, or extremist activity, enabling communities to capture safety alerts without exposing reporters. These reports are designed for local verification by trusted community leaders or moderators before wider distribution.

Records are stored locally and synchronized later when connectivity becomes available.

---

## C. Offline Community Sync

Jirani uses device-to-device synchronization concepts such as BLE/Wi-Fi Direct Ghost-Sync to preserve and distribute important information during low-connectivity situations.

The system supports:

- local peer-to-peer gossip syncing,
- delayed synchronization,
- lightweight encrypted transfers,
- local-first data storage,
- and resilient information sharing between nearby devices.

This enables continuity even during infrastructure instability.

---

## D. AI Translation & Accessibility

Jirani reduces language barriers during mediation and coordination by:

- translating simple community messages,
- simplifying technical or legal wording,
- supporting multilingual communication,
- and helping users express concerns respectfully.

---

## E. Trusted Information & Rumor Reduction

Communities can share verified local updates while reducing misinformation and panic escalation.

The AI assistant helps:

- summarize reports,
- identify uncertainty,
- encourage verification before escalation,
- and distinguish confirmed updates from unverified claims.

---

# 5. Security & Privacy Principles

Jirani is designed around community safety.

## Key Principles

- No mandatory phone numbers or government IDs
- No required central server for participation; peer-to-peer synchronization preserves local control
- Local-first encrypted storage
- Anonymous participation support, including anonymous safety and threat reporting
- Minimal metadata collection
- Optional recovery phrases for account restoration
- Community-controlled sensitive information

Sensitive information remains controlled by the community whenever possible.

---

# 6. Technical Architecture

| Layer | Technology |
|---|---|
| **Android App** | Kotlin + Jetpack Compose |
| **Local Storage** | SQLite / Room with encrypted local storage |
| **Core Logic** | Kotlin, with Rust modules where strong cryptographic or synchronization guarantees are needed |
| **AI Layer** | Hybrid AI: on-device mediation logic with optional cloud-assisted prompt orchestration for prototype/demo scenarios |
| **Offline Sync** | Bluetooth Low Energy / Wi-Fi Direct peer sync and Ghost-Sync concepts for local peer synchronization |
| **Backend** | Rust API for optional cloud synchronization, analytics, and aggregation |
| **Web Demo** | Lightweight web version for global judging and testing |

An optional central Rust server supports aggregated analytics, anonymized community insights, and gateway-based synchronization for connected environments. The core mobile experience remains fully functional offline and peer-to-peer, with the Rust backend used only when users opt into cloud-assisted analysis or reporting.

## Device Sharing & Peer Sync Technologies

Jirani supports local device-to-device sharing through platform-specific APIs and concepts:

- **Android**: system sharing with `Intent.ACTION_SEND` for user-driven file/text exchange, plus local peer sync using `Nearby Connections`, `Wi-Fi Direct`, or `Bluetooth Low Energy` for offline data gossiping.
- **iOS**: similar peer-sharing capabilities via `MultipeerConnectivity` for local peer discovery and data transfer, with `UIActivityViewController` supporting system-level sharing.

This cross-platform approach preserves offline-first resilience while allowing optional user-driven sharing for lightweight data exchange.

The detailed information flow from device-to-device sync queues to the optional OSF-hosted Rust analytics gateway is documented in `docs/INFORMATION_FLOW.md`, including behavior when BLE, Nearby Share, or Wi-Fi Direct are enabled or disabled.

## Why Kotlin + Jetpack Compose

Jirani prioritizes performance and usability on low-spec Android devices. Kotlin with Jetpack Compose provides a native Android experience with better performance, smaller runtime overhead, and deeper access to device capabilities compared to heavier cross-platform frameworks.

This is important because the target users may have:

- budget Android devices,
- limited storage,
- low battery capacity,
- unreliable connectivity,
- and older hardware.

---

# 7. Codex Integration Strategy

Codex is embedded as a first-class workflow partner across planning, development, review, and documentation rather than being bolted on after implementation. This project also leverages repository-level Codex tooling and agent-driven workflow artifacts such as `AGENTS.md`, prompts, and structured planning notes to make the integration visible and repeatable.

Codex is integrated throughout the project lifecycle:

| Area | Codex Usage |
|---|---|
| **Planning** | Structuring mediation flows and system architecture |
| **Development** | Generating API routes, Jetpack Compose UI scaffolding, and local storage logic |
| **AI Prompt Design** | Refining mediation, translation, and de-escalation prompts |
| **Documentation** | Generating technical documentation and onboarding guides |
| **Testing** | Assisting with scenario generation, edge-case simulation, and safety checks |
| **Review** | Supporting code review criteria, privacy checks, and design alignment |

---

# 8. Human Impact Scenarios

## Scenario 1 — Water Resource Conflict

Two neighboring pastoralist communities dispute access to a shared water source.

Jirani helps:

- document agreements offline,
- guide mediation discussions,
- record agreed sharing schedules,
- and synchronize records later when connectivity is restored.

---

## Scenario 2 — Displacement Coordination

A displaced family receives trusted offline information about:

- safe routes,
- nearby aid points,
- available community support,
- and trusted local contacts.

---

## Scenario 3 — Community Tension De-escalation

A local disagreement risks escalating into violence.

Jirani:

- suggests calm communication approaches,
- generates neutral language,
- proposes mediation steps,
- and encourages safe escalation to trusted local leaders when needed.

---

# 9. MVP Scope

For the capstone demo, Jirani will focus on a realistic and testable MVP:

## MVP Features

1. **AI Mediation Assistant**
   - Users describe a conflict.
   - The assistant suggests peaceful next steps and respectful communication.

2. **Agreement Generator**
   - Creates neutral agreement drafts using labels like Party A and Party B.
   - Preserves anonymity by avoiding mandatory names or phone numbers.

3. **Offline Agreement Records**
   - Saves agreements locally on the device.
   - Demonstrates delayed sync using simulated or local peer-sync behavior.

4. **Multilingual Support**
   - English and Swahili for the MVP.
   - Designed for future Somali, Arabic, French, Portuguese, and local-language expansion.

5. **Optional Analytics Backend**
   - A Rust-based analytics and aggregation server is contemplated as a later-stage extension for optional reporting and insight, not a required part of the core MVP.

---

# 10. Assumptions

- Users have access to Android smartphones with Bluetooth capability.
- Community leaders are willing to adopt lightweight digital coordination tools.
- Physical movement between users enables delayed synchronization opportunities.
- AI-generated mediation guidance is advisory and community-driven, not authoritative.
- Sensitive agreements may be formalized offline with trusted elders, mediators, chiefs, or community leaders.
- Anonymous safety reports should be verified through trusted local channels to avoid false alarms or misuse.

---

# 11. Trade-offs

| Trade-off | Decision |
|---|---|
| **Latency vs Resilience** | Delayed synchronization is acceptable in exchange for functionality during outages. |
| **Battery vs Connectivity** | Synchronization intervals are optimized to reduce battery drain. |
| **Security vs Simplicity** | Recovery phrases improve anonymity but require careful user education. |
| **Offline-first vs Real-time Cloud Features** | Local resilience is prioritized over continuous connectivity. |
| **On-device AI vs cloud assistance** | On-device mediation logic is preferred for privacy and offline use, with optional cloud-based prompt orchestration as a prototype fallback. |
| **Native Android vs Cross-platform** | Kotlin + Jetpack Compose is preferred for performance, smaller overhead, and better access to Android device capabilities. |

---

# 12. Long-Term Vision

Jirani aims to evolve into a scalable PeaceTech platform adaptable across regions experiencing:

- displacement,
- resource conflict,
- weak connectivity,
- community tension,
- humanitarian recovery needs,
- or institutional instability.

An optional Rust-based backend may be added later to provide centralized analytics, anonymized trend aggregation, and optional cloud sync gateways for communities that want additional insight or cross-region reporting. This backend is explicitly optional and does not replace the core decentralized, offline-first functionality.

Future directions include:

- NGO and humanitarian integrations,
- trusted community moderator tools,
- multilingual expansion,
- stronger offline synchronization capabilities,
- optional WhatsApp integration,
- and context-specific mediation modules for different regions.

---

# 13. Closing Statement

Jirani is designed around a simple belief:

> Peace is not only built through institutions — it is built daily between neighbors.

By combining offline-first resilience, AI-assisted mediation, multilingual accessibility, native Android performance, and community-centered design, Jirani provides practical tools for communication, trust-building, and peaceful coordination in challenging environments.
