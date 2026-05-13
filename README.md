# Jirani Project

Jirani is an offline-first PeaceTech project for careful community conflict reporting, trusted local verification, elder-led mediation support, and optional minimized report synchronization.

This repository is the main monorepo for the project. It contains the Android application and the optional Rust backend server while preserving the commit history from the original standalone repositories.

## Repository Layout

| Path | Purpose |
|---|---|
| `jirani/` | Android app built with Kotlin and Jetpack Compose. This is the primary user-facing application. |
| `jirani-rust/` | Optional Rust/Rocket backend gateway for minimized report sync, relay storage, and hosted testing. |
| `README.md` | This reviewer and contributor guide for the full monorepo. |

The detailed setup, development, and run instructions live inside each project:

- Android app: [`jirani/README.md`](jirani/README.md)
- Rust backend: [`jirani-rust/README.md`](jirani-rust/README.md)

## For Reviewers

Start here if you are reviewing the source code, project structure, or development approach.

1. Read this file to understand the monorepo.
2. Open [`jirani/README.md`](jirani/README.md) for the Android app overview and setup.
3. Open [`jirani-rust/README.md`](jirani-rust/README.md) for the optional backend gateway.
4. Review the project docs in each subdirectory, especially files such as `CAPSTONE_SPEC.md`, `ARCHITECTURE.md`, and `docs/` where available.

The Android app is designed to work offline-first. The Rust backend is optional and should be understood as a companion gateway, not a hard requirement for the app to function.

## Source Code

Clone the monorepo:

```bash
git clone https://github.com/ewanyonyi/Jirani-Project.git
cd Jirani-Project
```

Explore the Android app:

```bash
cd jirani
```

Explore the Rust backend:

```bash
cd jirani-rust
```

## Development Setup

Each project can be developed independently from its own subdirectory.

For Android development:

```bash
cd jirani
```

Then follow [`jirani/README.md`](jirani/README.md). In general, reviewers and contributors should open this project in Android Studio, build with Gradle, and run on an emulator or physical Android device.

For backend development:

```bash
cd jirani-rust
```

Then follow [`jirani-rust/README.md`](jirani-rust/README.md). In general, contributors should use a Rust 2021 toolchain and Cargo. Docker Compose is optional for local PostgreSQL testing.

## Typical Local Checks

Run Android checks from the Android project:

```bash
cd jirani
./gradlew test
```

Run Rust checks from the backend project:

```bash
cd jirani-rust
cargo fmt -- --check
cargo test
```

Some checks may require platform tooling such as Android Studio, an Android SDK, a configured emulator, Rust, Cargo, or Docker depending on what you are testing.

## Contributing

Contributions should be scoped to the project they affect:

- Android app changes should usually stay under `jirani/`.
- Backend changes should usually stay under `jirani-rust/`.
- Cross-project documentation or coordination changes can be made at the repository root.

Before opening a pull request:

1. Read the relevant project README.
2. Make the smallest focused change that solves the issue.
3. Run the relevant local checks for the project you changed.
4. Update documentation when behavior, setup, configuration, or reviewer expectations change.
5. Keep privacy and safety expectations in mind, especially for reporting, synchronization, mediation, domestic violence, and GBV-related flows.

## Monorepo History

The two projects were imported with `git subtree`, which keeps their original commit histories visible inside this repository.

View Android app history:

```bash
git log -- jirani
```

View Rust backend history:

```bash
git log -- jirani-rust
```

The original source repositories were:

- `https://github.com/ewanyonyi/jirani.git`
- `https://github.com/ewanyonyi/jirani-rust.git`

This monorepo should be treated as the main source of truth for future combined development unless the project maintainers decide otherwise.

## Project Status

Jirani is a prototype and capstone-style project. The Android app remains offline-first, and the backend gateway is optional infrastructure for hosted or partner-assisted sync experiments.

Do not treat prototype server deployments, demo tokens, or sample configuration values as production-ready community infrastructure without a full security, privacy, and operational review.
