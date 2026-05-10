# Development Plan for Jirani App

This plan follows the actual build order for the Jirani PeaceTech capstone. We started with the app UI screens, moved into reporting, are now focusing on sync/report movement, and will leave mediation/resolution for the final feature phase.

The workflow remains: write → test → review → document.

---

## Phase 1: App UI Screens

Goal: Build a usable native Android shell that can support the capstone demo.

- Define the main Jetpack Compose navigation structure.
- Build core screens:
  - Report
  - Agreements
  - Sync
  - Mediation
  - Settings
  - Decoy/quick-exit screen
- Create bottom navigation for primary app sections.
- Keep side drawer for secondary utilities only, avoiding duplicate bottom-nav links.
- Lock the app to portrait for stable demo and field use.
- Apply the Jirani visual direction: paper-like background, dark green brand color, clear touch targets, readable typography.

Status:
- [x] Main navigation shell
- [x] Bottom navigation
- [x] Side drawer cleanup
- [x] Portrait lock
- [x] Initial Compose screens

---

## Phase 2: Reporting Module

Goal: Make reporting the first real product flow, before mediation.

- Build the Report Incident screen.
- Use a horizontal progress bar for:
  - Details
  - Region
  - Send
- Create a 3-column incident category grid:
  - Violence
  - GBV
  - Resource
  - Rumor
  - Livestock
  - Domestic
  - Other
- Add category-specific colors and selected states.
- Add validation:
  - category required
  - description must be at least 10 characters
- Add approximate-area input.
- Add microphone icon placeholder for future voice input.
- Save UI state through `SavedStateHandle` so inputs survive rotation/process recreation.
- Clear fields after submit.
- Show a simple submission receipt instead of long triage text.
- Treat GBV/domestic reports as survivor-centered and not community-broadcast reports.

Status:
- [x] Report-first flow
- [x] Category grid
- [x] Horizontal progress bar
- [x] Validation
- [x] Saved ViewModel state
- [x] Submission receipt
- [x] GBV/domestic safety handling

---

## Phase 3: Sync & Report Movement

Goal: Make submitted reports move securely and anonymously from one Jirani device to trusted nearby devices.

Current focus.

- Convert submitted reports into local records.
- Strip report content into sanitized payloads.
- Create sync envelopes with:
  - random envelope ID
  - content hash
  - sensitivity class
  - allowed transports
  - delivery count
  - stale/expiry behavior
- Demonstrate secure packet movement using:
  - AES-GCM sealed payloads for MVP simulation
  - content-hash integrity checks
  - trusted nearby device simulation
- Send eligible reports automatically when trusted devices are available.
- If no device is available, keep the report queued and scanning-ready.
- Relay each eligible report to a maximum of five unique trusted devices.
- Stop relay when report becomes stale.
- Show saved/submitted reports with `Sent to X/5 devices`.
- Keep GBV/domestic reports out of nearby broadcast.

Status:
- [x] Sanitized report payload
- [x] Sync envelope model
- [x] AES-GCM MVP packet sealing
- [x] Integrity verification
- [x] Five-device relay limit
- [x] Stale report stop rule
- [x] Submitted reports delivery count
- [ ] Replace simulation with Android Nearby Connections adapter
- [ ] Add Wi-Fi Direct handoff path
- [ ] Add QR/encrypted file handoff for sensitive reports

---

## Phase 4: Agreements & Local Records

Goal: Preserve neutral outcomes and local records after reports are reviewed.

- Improve the Agreements screen.
- Store draft agreement records locally.
- Use neutral labels such as Community A and Community B.
- Prepare Room/SQLite entities for reports, sync envelopes, and agreements.
- Add encrypted storage direction before production use.
- Link report outcomes to agreement records only when safe.

Status:
- [x] Initial agreement screen
- [ ] Room/SQLite models
- [ ] Encrypted storage implementation
- [ ] Report-to-agreement linking rules

---

## Phase 5: Mediation / Resolution

Goal: Add mediation last, only after reporting, safety triage, and trusted review exist.

Mediation must not be the first action in a dangerous case.

- Redesign mediation as a second-stage review tool.
- Block mediation for:
  - active violence
  - retaliation risk
  - GBV/domestic reports
  - unverified rumors
- Support elders, peace committees, chiefs, religious leaders, and OSF/community partners.
- Generate neutral questions and agreement drafts.
- Help summarize decisions without names or blame.
- Add mediation/resolution tests.

Status:
- [x] Initial mediation agent
- [x] Safety warning behavior
- [ ] Full mediation review flow
- [ ] Resolution workflow
- [ ] Agreement handoff from reviewed cases

---

## Phase 6: Testing, Review, and Demo

Goal: Keep the MVP stable, privacy-aware, and demo-ready.

- Run unit tests regularly:
  - `./gradlew testDebugUnitTest`
- Add UI/instrumentation tests for:
  - report flow
  - sync status
  - submitted reports list
  - mediation safety blocking
- Validate privacy:
  - no names required
  - no exact GPS required
  - no phone numbers in sync payloads
  - GBV/domestic reports do not broadcast
- Prepare a 3-5 minute hackathon demo:
  - submit report
  - show anonymized sync
  - show sent-to-device count
  - show mediation blocked until safe

Status:
- [x] Unit tests for reporting and sync policy
- [x] Unit tests passing with `./gradlew testDebugUnitTest`
- [ ] Instrumentation tests
- [ ] Demo script
- [ ] Final review pass

---

## Current Priority

We are currently in **Phase 3: Sync & Report Movement**.

Next work:

1. Polish the Sync screen as the saved-reports and delivery-status area.
2. Make the simulated trusted-device flow clearer for the demo.
3. Prepare the future adapter boundary for Android Nearby Connections.
4. Keep mediation/resolution as the last major phase.
