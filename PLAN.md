# Development Plan for Jirani App

This plan follows the Codex-style development workflow for the Jirani PeaceTech app: plan → build → test → review. It aligns with the current `CAPSTONE_SPEC.md` and `AGENTS.md` by centering on offline-first mediation, anonymous reporting, and native Kotlin/Jetpack Compose development.

## Phase 1: Planning & Setup
- Review the capstone vision, constraints, and MVP scope from `CAPSTONE_SPEC.md`.
- Confirm agent responsibilities from `AGENTS.md`:
  - Mediation Agent
  - Translation Agent
  - Summary Agent
  - Reporting Agent
- Set up Android Studio / IntelliJ and VS Code with Codex/Copilot.
- Verify Gradle wrapper, Kotlin toolchain, and local Android SDK.
- Create or update project config files including `config.toml` and `scripts/README.md`.

**Prompt Reference**: Use `prompts/initial_prompt.md` for overall Codex guidance and project setup.

## Phase 2: Core Architecture
- Define local storage models using Room/SQLite and encrypted storage patterns.
- Design Jetpack Compose screens for:
  - dispute description and mediation assistance,
  - agreement record creation,
  - anonymous incident reporting,
  - local sync status and history.
- Implement the offline-first sync design:
  - local peer-to-peer data flow concepts,
  - delayed synchronization and data gossiping,
  - placeholder support for BLE/Wi-Fi Direct / Ghost-Sync.
- Build the AI mediation layer with hybrid support:
  - on-device guidance and prompt-driven logic,
  - optional cloud-assisted prompt orchestration for prototypes.

**Prompt Reference**: Use `prompts/initial_prompt.md` for architecture design and MVVM setup.

## Phase 3: Feature Development
- Mediation Assistant: accept conflict descriptions and generate calm, neutral responses.
- Community Agreement Records: save peace agreements, schedules, and coordination notes locally.
- Anonymous Reporting: support threat reports for goons, rustlers, terrorism, and local security alerts.
- Translation & Accessibility: integrate English and Swahili support with future extensibility.
- Reporting Workflows: include trusted verification guidance for anonymous safety alerts.

**Prompt Reference**: Use `prompts/mediation_prompt.md` for mediation features, `prompts/translation_prompt.md` for translation, `prompts/agreement_summary_prompt.md` for summaries, and `prompts/reporting_prompt.md` for reporting.

## Phase 4: Testing & Validation
- Implement Android unit tests for business logic and data models.
- Implement instrumentation/UI tests for Compose screens and user flows.
- Run commands from `AGENTS.md`:
  - `./scripts/install-debug.sh`
  - `./scripts/list-devices.sh`
  - `./gradlew testDebugUnitTest`
  - `./gradlew connectedAndroidTest`
  - `./gradlew assembleDebug`
  - `./scripts/build-release-aab.sh`
  - `./scripts/monitor-logs.sh`
- Validate privacy and security behavior:
  - anonymous participation,
  - no PII collection,
  - encrypted local storage,
  - safe reporting verification.

**Prompt Reference**: Use `prompts/initial_prompt.md` for testing guidance and validation.

## Phase 5: Review & Iteration
- Use the SDLC mini-loop: write → test → review on each feature.
- Review against Google Android code review guidance in `AGENTS.md`.
- Update documentation and `CAPSTONE_SPEC.md` for new feature changes.
- Iterate on scope to keep the MVP focused and demo-ready.

**Prompt Reference**: Use `prompts/initial_prompt.md` for review and iteration guidance.

## Phase 6: Demo Preparation
- Prepare a sharp 3–5 minute walkthrough highlighting:
  - the problem and real-world impact,
  - the offline-first solution,
  - Codex integration across workflow,
  - the MVP features and future roadmap.
- Ensure the app runs on low-spec Android devices or emulator.
- Document the optional Rust backend as a later-stage analytics and sync gateway.

**Prompt Reference**: Use `prompts/initial_prompt.md` for demo preparation and final validation.

## Development Checklist
This checklist guides Codex through iterative development, testing, and reviewing. Check off items as completed, and iterate using the SDLC mini-loop (write → test → review).

### Setup & Planning
- [ ] Review `CAPSTONE_SPEC.md` and `AGENTS.md` for alignment.
- [ ] Set up Android Studio/IntelliJ and VS Code with Codex/Copilot.
- [ ] Verify Gradle wrapper, Kotlin toolchain, and Android SDK.
- [ ] Create/update `config.toml` and scripts in `scripts/` directory.

**Prompt Reference**: `prompts/initial_prompt.md`

### Core Architecture
- [ ] Define Room/SQLite models for local storage (disputes, agreements, reports).
- [ ] Implement encrypted storage for sensitive data.
- [x] Design initial Jetpack Compose screen for mediation assistance.
- [ ] Design Jetpack Compose screens for agreements, reporting, and sync status.
- [ ] Implement offline-first sync placeholders (local data flow, delayed sync simulation).
- [x] Build initial AI mediation layer with prompt-driven logic and rule-based fallback.
- [x] Add first agent-first domain package for mediation, reporting, summary, and translation.

### Feature Implementation
- [ ] Mediation Assistant: Accept conflict descriptions and generate neutral responses.
- [ ] Community Agreement Records: Save agreements, schedules, and notes locally.
- [ ] Anonymous Reporting: Support threat reports with verification guidance.
- [ ] Translation: Integrate English/Swahili support with extensibility.
- [ ] Reporting Workflows: Ensure anonymous safety alerts with trusted checks.

**Prompt Reference**: `prompts/mediation_prompt.md`, `prompts/translation_prompt.md`, `prompts/agreement_summary_prompt.md`, `prompts/reporting_prompt.md`

### Testing & Validation
- [x] Write initial unit tests for mediation and reporting agent logic.
- [ ] Write unit tests for data models after Room entities are introduced.
- [ ] Write instrumentation/UI tests for Compose screens and user flows.
- [ ] Run `./scripts/install-debug.sh` to install on device/emulator.
- [ ] Run `./scripts/list-devices.sh` to verify devices.
- [ ] Execute `./gradlew testDebugUnitTest` for unit tests.
- [ ] Execute `./gradlew connectedAndroidTest` for UI tests.
- [ ] Build debug APK with `./gradlew assembleDebug`.
- [ ] Build release AAB with `./scripts/build-release-aab.sh`.
- [ ] Monitor logs with `./scripts/monitor-logs.sh`.
- [ ] Validate privacy: No PII, encrypted storage, anonymous participation.
- [ ] Test on low-spec devices/emulators for performance.

**Prompt Reference**: `prompts/initial_prompt.md`

### Review & Iteration
- [ ] Follow SDLC mini-loop: Write code, test, review for each feature.
- [ ] Review code against Google Android guidelines in `AGENTS.md`.
- [ ] Update `CAPSTONE_SPEC.md` and docs for changes.
- [ ] Iterate on MVP scope to stay focused.
- [ ] Prepare demo walkthrough and ensure app runs smoothly.

**Prompt Reference**: `prompts/initial_prompt.md`

## Risks & Mitigations
- **Peer-to-peer sync complexity**: Focus the MVP on local storage and delayed sync simulation first, then add BLE/Wi-Fi Direct concepts later.
- **On-device AI limitations**: Build prompt-driven mediation logic with a simple rule-based fallback for offline use.
- **Anonymity vs trust**: Keep personal identifiers optional and provide trusted verification guidance for safety reports.
- **Low-spec device performance**: Optimize Compose UI and reduce background sync work; test on older/emulator hardware early.
- **Scope creep**: Maintain the MVP boundary around mediation, agreement records, anonymous reporting, and basic translation.
