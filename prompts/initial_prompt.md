You are a senior Android architect and AI coding agent.

I already have an Android Studio project generated. Do **not** recreate or replace the Gradle project.

Current package:

`com.jirani.app`

Current source root:

`app/src/main/java/com/jirani/app/`

Your task is to add Jirani documentation, planning files, VS Code config, prompts, and the correct Android package directory structure inside the existing project.

# Do NOT overwrite

Do not overwrite these existing files unless only adding safe minimal changes:

* `settings.gradle.kts`
* `build.gradle.kts`
* `app/build.gradle.kts`
* `gradle/libs.versions.toml`
* `MainActivity.kt`
* `AndroidManifest.xml`
* existing theme files
* Gradle wrapper files

# Create root documentation files

Create these at the project root:

* `README.md`
* `CAPSTONE.md`
* `TASKS.md`
* `PLAN.md`
* `AGENTS.md`
* `SYSTEM_PROMPT.md`
* `ARCHITECTURE.md`
* `API_CONTRACTS.md`
* `CONTRIBUTING.md`
* `.env.example`

# Create docs folder

Create:

```text
docs/
├── MVP_SCOPE.md
├── SECURITY.md
├── OFFLINE_FIRST.md
└── DATA_MODEL.md
```

# Create prompts folder

Create:

```text
prompts/
├── mediation_prompt.md
├── agreement_summary_prompt.md
└── translation_prompt.md
```

# Create VS Code config

Create:

```text
.vscode/
├── extensions.json
└── settings.json
```

# Create Android package directories

Inside:

`app/src/main/java/com/jirani/app/`

create this structure:

```text
data/
├── local/
├── remote/
├── repository/
└── sync/

domain/
├── model/
├── repository/
└── usecase/

ui/
├── screens/
├── components/
├── navigation/
└── theme/   # already exists, preserve existing files

viewmodel/
di/
util/
```

# Add placeholder files

In each new empty package directory, add a `.gitkeep` file so Git tracks the folder.

Do not add Kotlin implementation files yet unless they are simple placeholders and do not affect compilation.

# PLAN.md requirement

Create `PLAN.md` using OpenAI Codex ExecPlan style.

It must include:

* Purpose / Big Picture
* Progress
* Surprises & Discoveries
* Decision Log
* Outcomes & Retrospective
* Context and Orientation
* Plan of Work
* Concrete Steps
* Validation and Acceptance
* Future Milestones

The file must be self-contained so a future Codex agent can continue the project without reading this conversation.

# AGENTS.md requirement

Create `AGENTS.md` with project rules for Codex.

Include:

* Use Kotlin and Jetpack Compose only
* Preserve Android Studio generated structure
* Prefer offline-first design
* Use Room for local persistence
* Use StateFlow instead of LiveData
* Keep code lightweight for low-end Android devices
* Use ExecPlans for complex features or refactors
* Do not overwrite existing Gradle files unless explicitly requested

# Documentation content

Populate the markdown files with practical starter content for Jirani.

Jirani is an offline-first peace coordination and mediation Android app for communities affected by conflict, disaster, or institutional breakdown.

The future stack is:

* Kotlin
* Jetpack Compose
* Material 3
* Room
* Coroutines + Flow
* Ktor Client
* Kotlin Serialization
* Hilt

The docs should explain:

* Offline-first strategy
* Android-first architecture
* Why Kotlin + Jetpack Compose
* Security and privacy expectations
* AI mediation assistant concept
* Local agreement recording
* Incident and resource reporting
* Future sync strategy
* MVP roadmap

# VS Code files

`.vscode/extensions.json` should recommend:

* Kotlin
* Gradle for Java
* Error Lens
* GitLens
* Docker

`.vscode/settings.json` should include sensible formatting defaults.

# Final output

After creating the files, summarize:

1. Files created
2. Directories created
3. Files intentionally preserved
4. Recommended next Codex task

Start immediately.
