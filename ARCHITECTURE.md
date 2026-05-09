# Architecture of Jirani App

## Overview
The Jirani app follows an MVVM (Model-View-ViewModel) architecture with an offline-first design, emphasizing decentralized sync, privacy, and on-device AI for PeaceTech mediation and reporting. It uses Kotlin and Jetpack Compose for native Android performance on low-spec devices. The app operates without central servers, relying on local storage and optional peer-to-peer sync.

This architecture directly supports the capstone vision by enabling:
- conflict prevention through AI-assisted mediation,
- community agreement recording and recovery workflows,
- anonymous threat reporting for local safety,
- and low-connectivity resilience using peer-to-peer sync concepts.

## Architectural Principles
- **Offline-First**: All core features work without internet; data syncs when connectivity allows.
- **Decentralized Sync**: Implements Ghost-Sync concepts for peer-to-peer data sharing via BLE/Wi-Fi Direct.
- **Privacy & Security**: Anonymous participation, encrypted local storage, no PII collection.
- **Modular Agents**: Independent features (Mediation, Translation, Summary, Reporting) coordinated through UI.
- **On-Device AI**: Prompt-driven logic for mediation, with rule-based fallbacks.
- **Extensibility**: Optional Rust backend for analytics and aggregation in a separate repo.

## Components
The app is structured into modular agents, each handling a specific PeaceTech function:

- **Mediation Agent**: Handles dispute mediation, de-escalation guidance, and neutral language suggestions.
- **Translation Agent**: Manages language translations (English, Swahili) with future extensibility.
- **Summary Agent**: Generates agreement summaries, neutral dispute records, and community decision notes.
- **Reporting Agent**: Enables anonymous reporting of security threats (goons, terrorists, cattle rustlers) with trusted verification.

Agents are coordinated through the UI and local logic, without a central orchestrator.

Current implementation starts with lightweight domain agents in `app/src/main/java/com/jirani/app/domain/agent/` and a mediation UI slice in `app/src/main/java/com/jirani/app/ui/mediation/`. These agents are deterministic offline fallbacks that can later be backed by Room records, richer prompts, or optional cloud-assisted orchestration.

## Layers
- **Data Layer**: Local storage using Room/SQLite with encryption. Handles disputes, agreements, reports, and sync metadata.
- **Domain Layer**: Business logic and use cases for mediation, translation, summarization, and reporting. Includes on-device AI prompts and rule-based processing.
- **UI Layer**: Jetpack Compose screens for user interactions, including dispute input, agreement creation, reporting forms, and sync status.

## Package Direction
- `domain/agent`: Agent-first mediation, reporting, summary, and translation logic.
- `ui/mediation`: Compose mediation flow and guidance rendering.
- `data/local`: Future Room entities, DAOs, and encrypted persistence helpers.
- `sync`: Future sync envelope creation and peer transport adapters.

## Sync Layer
- **Ghost-Sync Model**: Decentralized peer-to-peer sync using BLE/Wi-Fi Direct for data gossiping and delayed synchronization.
- **Local-First**: Prioritizes local data; sync is opportunistic and conflict-free.
- **Optional Backend**: Rust-based gateway for analytics and aggregation (separate repo).

## Security
- Encrypted storage for all sensitive data.
- Anonymous reporting with optional trusted verification.
- No external data transmission without user consent.
- Adheres to privacy principles: no PII, safe participation.

## Technologies
- **Language**: Kotlin 1.x (aligned with project Gradle).
- **UI Framework**: Jetpack Compose for declarative, performant UI.
- **Database**: Room/SQLite with SQLCipher for encryption.
- **Architecture Components**: ViewModel, LiveData/Flow for state management.
- **Sync**: BLE/Wi-Fi Direct APIs for peer-to-peer.
- **AI**: On-device prompt processing (e.g., via ML Kit or custom logic).
- **Build**: Gradle with Kotlin DSL.
- **Testing**: Android unit tests, instrumentation tests.
- **Optional Backend**: Rust 2021 edition with Cargo (separate repo).

## Architectural Diagrams
The following wireframe structures describe the app's architecture using plain-text ASCII diagrams.

### Overall Architecture Flow
```
User Interface (Jetpack Compose)
            |
            v
         ViewModel
            |
            v
  Domain Layer - Business Logic
            |
            v
      Data Layer - Room/SQLite
            |
            v
Sync Layer - BLE/Wi-Fi Direct
            |
            v
 Optional Rust Backend (separate repo)

Business Logic --> AI Mediation (On-Device Prompts)
```

### Agent Interactions
```
          +-------------------+
          |     UI Layer      |
          +-------------------+
            /       |       \
           v        v        v
+----------------+ +----------------+ +----------------+
| Mediation      | | Translation    | | Summary        |
| Agent          | | Agent          | | Agent          |
+----------------+ +----------------+ +----------------+
            \       |       /
             v      v      v
          +-------------------+
          |   Domain Logic    |
          +-------------------+
                  |
          +-------------------+
          |    Database       |
          +-------------------+
                  |
          +-------------------+
          |      Sync         |
          +-------------------+
                  |
          +-------------------+
          |  Optional Backend |
          +-------------------+
```

### Data Flow
```
User Input
    |
    v
UI Screen
    |
    v
ViewModel
    |
    v
Use Case
    |
    v
Repository
    |
    v
Room DAO
    |
    v
SQLite Database
    |
    v
Encrypted Storage
    |
    v
Sync Queue
    |
    v
Peer-to-Peer Sync
```
