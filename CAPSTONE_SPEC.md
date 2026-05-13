# CAPSTONE PROJECT: PeaceTech Initiative

# Project Identity

## Jirani

> "An offline-first peace coordination platform that helps communities report conflict safely, verify facts locally, mediate only when safe, and preserve trusted agreements during conflict and displacement."

Derived from the Swahili word **Jirani** (neighbor), the name reflects the belief that peace is built and sustained at the community level through dialogue, trust, and cooperation.

---

# 1. Vision & Problem Statement

Jirani is an offline-first PeaceTech Android app designed to help communities:

- report conflict and threats safely;
- verify facts through trusted local actors;
- protect people before attempting dialogue;
- support elder-led mediation only when it is safe;
- preserve local agreements and follow-up records.

This project is designed for the Open Society Foundations PeaceTech challenge. It supports displaced, insecure, and multilingual communities with practical, low-connectivity tools that promote accountability, safety, privacy, and local agency.

Using recent Kamba-Somali conflict patterns in Kenya as a guiding case study, Jirani addresses the gap between early warning and mediation. Reports about livestock movement, grazing pressure, water access, rumors, retaliation, road closures, domestic violence, GBV, or attacks are captured first. Mediation comes later, only when trusted local actors decide people can meet safely.

This capstone stays aligned with the hackathon brief: it is an invention sprint, not a production launch. The demo should prove a working, realistic concept that is valuable enough to pursue, not claim to solve every conflict workflow end to end.

The platform is designed for environments where:

- internet access is unreliable;
- institutions may be inaccessible or slow to respond;
- trust is fragile;
- communities are displaced or recovering from conflict;
- users may face personal risk if their participation is exposed;
- people need simple tools to coordinate without depending on a central server.

---

# 2. Core PeaceTech Objectives

Jirani fits the **Peace & community** track from the capstone idea brief, with overlap into **Voice & accountability** because it helps people be heard safely without exposing identities.

## A. Safe Conflict Reporting

Help communities capture what happened without exposing reporters, survivors, or witnesses, and without turning rumors into public facts.

## B. Local Verification & Protection

Separate active danger, rumor, survivor-centered harm, and mediation-ready resource disputes so the correct people act first.

## C. Elder-Led Mediation & Recovery

Support accepted local actors in documenting peaceful resolutions, community decisions, resource-sharing agreements, and follow-up.

---

# 3. Operating Constraints & Design Response

| Constraint | Jirani Response |
|---|---|
| **Displaced populations** | Mobile-first experience optimized for unstable environments and low-spec Android devices. |
| **Low bandwidth** | Offline-first records and delayed nearby sharing when connectivity or trusted devices are available. |
| **Secure access / anonymity** | No mandatory phone numbers, government IDs, exact GPS, or personal profiles. |
| **Privacy risk** | Sanitized sync payloads, anonymous labels, local-first storage, and survivor-centered handling for GBV/domestic reports. |
| **Decentralized resilience** | Trusted nearby sharing through Android-native handoff paths before optional gateway upload. |
| **Multilingual communities** | English and Swahili first, with Somali and Kamba as priority expansion languages. |
| **Adaptability across regions** | Modular report categories, mediation flows, local actor labels, and agreement templates. |

---

# 4. Core Product Flow

## Step 1: Report What Happened

The app starts with reporting, not mediation. A community member can record:

- what happened;
- approximate area, not exact home or GPS;
- time window;
- whether the information was observed, heard as rumor, or received from another person;
- immediate risk such as violence, retaliation, livestock movement, blocked water, road closure, hate speech, property destruction, domestic violence, or GBV.

The report form must not ask for names, phone numbers, ID numbers, exact homes, or photos of faces.

## Step 2: Local Review & Triage

Jirani classifies the next action:

- **Protection first:** active attack, weapons, killings, arson, road danger, or revenge calls.
- **Verify first:** rumor, incitement, or unconfirmed claim.
- **Elder review:** grazing, water, boundary, livestock, market, or resource dispute where people may be able to meet safely.
- **Survivor support only:** domestic violence, GBV, sexual violence, defilement, or intimate partner abuse.

Domestic violence and GBV reports are not mediation cases and are not community alerts. They stay local by default and may be shared only with a survivor-chosen support actor or trained protection focal person.

## Step 3: Mediation Review

Mediation is a second-stage tool. It becomes available only when trusted actors decide:

- both sides can be contacted safely;
- the issue is negotiable;
- the meeting place is neutral;
- elders or peace actors from affected communities accept the process;
- there is no active attack, revenge mobilization, or survivor-safety risk.

Jirani helps local actors write neutral questions, summarize concerns without blame, and define practical next steps such as temporary grazing routes, water schedules, apology processes, compensation review, market access, route safety, or follow-up meetings.

## Step 4: Agreement & Follow-Up

Agreements use anonymous labels such as Community A and Community B. Records should capture:

- what each side agreed to do;
- who will check compliance;
- review date;
- what happens if the agreement fails;
- who may safely receive the record.

---

# 5. Core Features

## A. Conflict, Threat, Domestic Violence, and GBV Reporting

Jirani supports careful reporting for:

- resource conflict;
- livestock and grazing disputes;
- rumors and incitement;
- active violence or retaliation risk;
- extremist or organized threats;
- domestic violence;
- GBV and survivor safety concerns.

## B. Survivor-Centered Safety Handling

Domestic violence and GBV reports follow stricter rules:

- no community broadcast;
- no mediation workflow;
- no notification to family, elders, police, or alleged abuser without survivor consent, unless child protection or immediate life-threatening danger requires urgent action;
- private handoff only to survivor-chosen support, trained GBV focal person, health provider, legal support provider, psychosocial support provider, or approved protection actor.

## C. Trusted Nearby Sharing

Jirani creates sanitized sync envelopes for reports and agreements. Shared payloads include only:

- report type;
- general area;
- time window;
- observed risk;
- verification status;
- sensitivity class.

Names, phone numbers, exact GPS, exact homes, device identifiers, and reporter identity are excluded.

Supported movement paths:

- **Google Nearby Connections:** trusted verifier exchange when nearby devices are present.
- **Wi-Fi Direct:** direct local transfer without internet.
- **Android Sharesheet:** explicit user-driven handoff to a trusted app or contact.
- **QR or encrypted file:** small sensitive payloads, especially survivor-centered handoff.
- **Optional Jirani Server:** aggregated, minimized analytics only when a community opts in.

## D. Community Agreement Records

Communities can create and preserve:

- peace agreements;
- grazing schedules;
- water-sharing arrangements;
- local mediation outcomes;
- community recovery plans;
- aid/resource coordination notes.

## E. Translation & Accessibility

Jirani reduces language barriers by supporting simple wording across English and Swahili first, with Somali and Kamba prioritized next.

## F. Trusted Information & Rumor Reduction

The app helps distinguish confirmed updates from unverified claims, encourages local verification before escalation, and prevents broad sharing of unverified or sensitive reports.

---

# 6. Security & Privacy Principles

- No mandatory phone numbers or government IDs.
- No exact GPS for safety reports.
- No public publishing of raw reports.
- Local-first encrypted storage before production use.
- Random non-PII record IDs and sync envelope IDs.
- Minimal sync metadata, stored separately from report content.
- Survivor-centered reports remain local unless explicitly handed to a safe support actor.
- Optional gateway uploads use minimized or aggregated payloads only.

---

# 7. Technical Architecture

| Layer | Technology | Implementation Direction |
|---|---|---|
| **Android app** | Kotlin + Jetpack Compose | Native UI optimized for low-spec Android devices. |
| **Local storage** | Room / SQLite, encrypted before production | Local-first records, reports, agreements, and sync metadata. |
| **Domain logic** | Kotlin agents | Deterministic reporting, triage, mediation readiness, translation, and summary logic. |
| **Sync model** | Sanitized sync envelopes | Sensitivity-based audience tiers and transport rules. |
| **Nearby sharing** | Nearby Connections, Wi-Fi Direct, Android Sharesheet, QR/encrypted file | Trusted device-to-device movement without requiring internet. |
| **Optional backend** | Rust API | Aggregation, deduplication, and gateway handoff for opt-in communities. |
| **Future Rust modules** | Rust via JNI where needed | Cryptographic operations, packet verification, and performance-sensitive sync logic. |

The current implementation keeps the core experience local and deterministic. Production versions can replace the in-memory store with Room and add Android-native transport adapters while preserving the same data model and privacy policy.

---

# 8. Data Movement Summary

```text
Device A
  -> user creates report locally
  -> app creates SafetyReportRecord
  -> privacy filter creates SanitizedReportPayload
  -> SyncEnvelope adds audience tier, hash, version, and allowed transports
  -> trusted handoff path is selected
  -> Device B verifies duplicate/hash status
  -> trusted reviewer marks verification status
  -> eligible records move onward only if sensitivity policy allows
```

Sensitivity rules:

| Sensitivity | Example | Movement |
|---|---|---|
| **Community** | grazing dispute, water access, verified rumor | Can move to trusted verifiers through nearby sharing. |
| **Protection** | attack, armed group, retaliation risk | Moves only to trusted protection actors. |
| **Survivor-centered** | domestic violence, GBV, sexual violence | Held locally by default; private survivor-support handoff only. |

---

# 9. Codex Integration Strategy

Codex is embedded as a first-class workflow partner across planning, development, review, testing, and documentation.

| Area | Codex Usage |
|---|---|
| **Planning** | Reshaping mediation into a report-first, real-world conflict workflow. |
| **Development** | Generating Kotlin domain models, Compose UI updates, and sync-envelope logic. |
| **Prompt design** | Refining reporting, mediation, translation, and summary prompts. |
| **Documentation** | Maintaining capstone specs, data flow docs, security notes, and user guides. |
| **Testing** | Generating unit tests for privacy-sensitive reporting and sync behavior. |
| **Review** | Checking clarity, maintainability, privacy, and PeaceTech fit. |

---

# 10. Human Impact Scenarios

## Scenario 1: Kamba-Somali Resource Conflict

A report says camels crossed into farms, rumors of revenge are spreading, and people are gathering near a market road.

Jirani helps:

- capture the report without names;
- mark rumors as unverified;
- route active danger to protection first;
- allow elders and peace actors to open mediation only when both sides can meet safely;
- record a temporary grazing, compensation, or review agreement.

## Scenario 2: Domestic Violence or GBV Report

A survivor or trusted helper records that someone needs private help after domestic violence or GBV.

Jirani helps:

- keep the record local by default;
- avoid names, exact homes, photos, phone numbers, and community broadcast;
- block mediation;
- show survivor-support-only next steps;
- allow private handoff only to a survivor-chosen support actor or trained protection focal person.

## Scenario 3: Displacement Coordination

A displaced family receives trusted offline information about safe routes, nearby aid points, available community support, and local contacts.

---

# 11. MVP Scope

For the capstone demo, Jirani focuses on a realistic and testable MVP:

1. **Report-first flow:** conflict, threat, domestic violence, and GBV reports with approximate area, time window, and no PII.
2. **Local triage:** protection first, verify first, elder review, or survivor-support only.
3. **Mediation review:** guidance for elders and peace actors only after safety review.
4. **Sanitized sync envelopes:** sensitivity-based audience tiers and allowed movement paths.
5. **Agreement generator:** neutral agreement drafts without PII.
6. **Offline records:** local storage direction and simulated queue status before full Room implementation.
7. **Multilingual UI direction:** English and Swahili first, with Somali and Kamba as priority expansion languages.

Out of scope for MVP:

- mandatory account registration;
- central-server-only workflows;
- production-grade BLE/Wi-Fi Direct/Nearby implementation;
- production-grade on-device LLM inference;
- direct user-started mediation during active violence;
- community broadcast of domestic violence or GBV reports.

---

# 12. Hackathon Requirements Alignment

The hackathon asks each project to demonstrate four things:

| Requirement | Jirani Response |
|---|---|
| **Depth of Codex integration** | Codex is visible across planning, implementation, tests, docs, review criteria, and prompt design. Repo artifacts include `AGENTS.md`, prompts, architecture docs, information flow docs, and tested Kotlin logic. |
| **Real-world impact** | The product is grounded in concrete Kenyan conflict patterns: Kamba-Somali resource tension, cattle rustling risk, rumors, retaliation, displacement, domestic violence, and GBV reporting. |
| **Reusability & adoption potential** | The app uses native Android, simple modular agents, offline-first records, and documented sync envelopes so another developer can extend the workflow without needing a central server first. |
| **Demo & pitch quality** | The demo can show a 3-5 minute flow: submit a report, classify urgency, create a sanitized sync envelope, block unsafe mediation, and show survivor-support or protection next steps. |

The capstone idea brief also defines operating constraints. Jirani addresses them as follows:

| OSF Constraint | Demo Proof |
|---|---|
| **Displaced populations** | No account requirement; reports and agreements can be created on a shared or low-end Android device. |
| **Low bandwidth** | Reports are useful locally before internet exists; sync envelope logic prepares nearby/offline movement. |
| **Secure access / anonymity** | No names, phone numbers, exact GPS, or government IDs are required. |
| **Multilingual communities** | English and Swahili are first-class MVP targets, with Somali and Kamba as priority expansion languages. |
| **Adaptability** | Report categories, sensitivity rules, local actor labels, and mediation flows are modular. |

---

# 13. Trade-Offs

| Trade-off | Decision |
|---|---|
| **Speed vs safety** | Protection and verification come before mediation. |
| **Reach vs privacy** | Sensitive reports move less broadly, especially survivor-centered records. |
| **Latency vs resilience** | Delayed sync is acceptable in exchange for offline operation. |
| **Battery vs connectivity** | Nearby discovery should be duty-cycled in production. |
| **Native vs cross-platform** | Kotlin + Jetpack Compose is preferred for Android performance and access to local sharing APIs. |
| **Local-first vs analytics** | Local participation works without the optional Jirani Server. |

---

# 14. Long-Term Vision

Jirani can evolve into a scalable PeaceTech platform adaptable across regions experiencing:

- displacement;
- resource conflict;
- weak connectivity;
- community tension;
- domestic violence and GBV reporting needs;
- humanitarian recovery needs;
- institutional instability.

Future directions include:

- Room-backed encrypted storage;
- Android Nearby Connections transport adapter;
- Wi-Fi Direct handoff;
- encrypted QR/file export;
- trusted reviewer workflows;
- local-language expansion;
- optional Jirani Server for minimized analytics;
- context-specific modules for different communities.

---

# 15. Closing Statement

Jirani is designed around a simple belief:

> Peace is not only built through institutions. It is built daily between neighbors.

By combining report-first safety, local verification, survivor-centered privacy, elder-led mediation, trusted nearby sharing, and multilingual accessibility, Jirani provides practical tools for communication, protection, trust-building, and peaceful coordination in challenging environments.
