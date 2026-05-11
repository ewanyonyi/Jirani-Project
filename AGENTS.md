# AGENTS.md

## Setup
Prefer Android Studio or IntelliJ with the Gradle wrapper and Kotlin toolchain.

Use VS Code or Android Studio with Codex/Copilot-style AI assistance for Android development.

Use the repository structure and existing Gradle/Kotlin conventions for Android work.

For optional backend work, use Rust 2021 edition with Cargo and the project’s Rust toolchain.

For JavaScript or TypeScript demos, use Node LTS.

## Companion Rust Gateway
The optional Rust/Rocket gateway repository lives at:

```text
/home/ewanyonyi/dev/jirani-rust
```

The Android app must remain offline-first. Nearby Connections and local storage should continue working even when the Rust gateway is unavailable.

When changing remote sync behavior:

- Keep Android and Rust models aligned for `SyncEnvelope` and `SanitizedReportPayload`.
- Update `/home/ewanyonyi/dev/jirani/docs/REMOTE_RUST_GATEWAY.md`.
- Update `/home/ewanyonyi/dev/jirani-rust/docs/ANDROID_INTEGRATION.md`.
- Use `JIRANI_REMOTE_GATEWAY_URL` as a Gradle property or environment variable for hosted test servers.
- Prefer HTTPS for hosted testing. Do not broaden Android cleartext network config without a clear test-only reason.
- Preserve privacy rules: minimized payloads only, content-hash verification, no PII, and no survivor-centered GBV/domestic default gateway sync.
- Be explicit that direct HTTPS still exposes source IP at the network layer. Android should not send device IDs, precise location, reporter identity, or high-fingerprint headers. Use a trusted relay/proxy if source-IP anonymity from the gateway operator is required.

## Tools
- Android Studio / IntelliJ for native Android development.
- VS Code with Codex/Copilot plugin for code completion, prompt-driven guidance, and project workflow.
- Android SDK tools, `adb`, and emulator manager for local device runs.

## Style
Use idiomatic Kotlin and Jetpack Compose patterns for Android UI and architecture.

Prefer small diffs, clear naming, and modular components.

Follow the existing project conventions before introducing new ones.

For Rust backend code, use idiomatic Rust with strong type safety and clear module boundaries.

Use clear names for agents and components to reflect PeaceTech, mediation, sync, and privacy.

## Testing
Use Android unit tests and instrumentation tests for Kotlin code.


For this project, prioritize targeted tests for core mediation logic, anonymous reporting, sync behavior, and privacy safeguards.

Common commands:

- Install app on connected device or emulator:
  - `./scripts/install-debug.sh` for advanced install options
- List Android devices and emulators:
  - `./scripts/list-devices.sh`
  - or use `adb devices` and `emulator -list-avds` directly
- Run Android unit tests:
  - `./gradlew testDebugUnitTest`
- Run instrumentation/UI tests on available devices/emulators:
  - `./gradlew connectedAndroidTest`
- Build debug APK:
  - `./gradlew assembleDebug`
- Build release AAB:
  - `./scripts/build-release-aab.sh` for keystore setup and release bundle creation
- Monitor app logs:
  - `./scripts/monitor-logs.sh`

## Review
Follow Google Android Code Review Guidelines: focus on clarity, correctness, maintainability, and security in the PeaceTech context.

- **SDLC Mini-Loop**: Follow write → test → review for each feature to ensure iterative quality and early feedback.
- **Design & Architecture**: Ensure changes align with Jetpack Compose patterns, offline-first principles, and decentralized sync. Verify no unnecessary dependencies on central servers.
- **Functionality**: Confirm features work as intended, especially for mediation, anonymous reporting, and multilingual support. Test edge cases like low-connectivity scenarios.
- **Complexity**: Prefer simple, readable solutions. Avoid over-engineering for budget hardware constraints.
- **Testing**: Require unit and instrumentation tests for new code. Prioritize tests for privacy safeguards and sync behavior. Report all test runs, failures, and remaining risks.
- **Naming & Style**: Use clear, consistent Kotlin naming. Follow idiomatic Compose patterns and project conventions.
- **Security & Privacy**: Check for anonymous participation, encrypted storage, and no PII exposure. Validate threat reporting workflows.
- **Documentation**: Update relevant docs (e.g., CAPSTONE_SPEC.md) for new features or changes.
- **Diffs**: Always show diffs for multi-file changes before merging.
- **Feedback**: Be constructive, respectful, and solution-oriented. Keep useful existing guidance; refine it instead of deleting it.

## Jirani Agents

The Jirani agents are modular features within the Android app, coordinated through the UI and local logic without a central orchestrator or manager agent. Each agent handles a specific PeaceTech function independently, aligning with the offline-first, decentralized architecture.

### Mediation Agent
Handles dispute mediation processes, de-escalation guidance, and neutral language suggestions.

### Translation Agent
Manages language translations and multilingual support for Swahili, English, and future local languages.

### Summary Agent
Generates agreement summaries, neutral dispute records, and community decision notes.

### Reporting Agent
Handles anonymous reporting of security threats, such as goons, terrorists, cattle rustlers, enabling communities to capture safety alerts without exposing reporters.
