Fifth Jirani UI/UX Improvement Prompt for Codex

You are improving the existing **Jirani** Android app UI.

Jirani is an offline-first PeaceTech app built with **Kotlin + Jetpack Compose**. It helps communities resolve conflict, create local agreements, report safety concerns, and sync information in low-connectivity environments.

The target users include:
- distressed people in conflict or recovery situations,
- elderly users,
- people with poor visibility,
- low-literacy users,
- users on low-end Android phones,
- users with unreliable internet and power.

The app must remain:
- lightweight,
- fast,
- offline-first,
- easy to understand,
- accessible,
- calm and non-threatening.

## Main Design Goal

Refactor the UI so it feels like:

> A trusted community peace tool

Not like:

> A modern technical dashboard

Prioritize clarity, emotional calm, accessibility, and low cognitive load over visual complexity.

---

## 1. Typography and Readability

Increase text sizes across the app.

Use these minimum sizes:

- Body text: `18sp`
- Button text: `20sp`
- Important button text: `22sp`
- Section headings: `32sp` to `40sp`
- Small helper labels: avoid anything below `16sp`

Use readable font weights:
- Headings: bold or semi-bold
- Body: regular or medium
- Avoid thin text styles.

Ensure all text remains readable on:
- small screens,
- old Android phones,
- outdoor lighting,
- low-quality displays.

---

## 2. Improve Color Contrast

Current soft colors are calm but some areas are too low contrast.

Use a simple accessible palette:

- Background: warm off-white / light beige
- Primary: deep green
- Text: near-black / dark charcoal
- Secondary text: dark gray, not pale gray
- Warning/accent: muted amber or terracotta
- Avoid relying on pale purple for important actions

Rules:
- All important text must have strong contrast.
- Buttons must be clearly visible.
- Selected states must be obvious without relying only on subtle color changes.
- Do not use gradients, blur, glass effects, or heavy shadows.

---

## 3. Remove or Replace Floating Report Button

The floating “Report” button currently overlaps content and may confuse older or distressed users.

Replace it with clearer fixed actions:

Option A:
- Add “Create Safety Report” as a full-width button inside the Safety screen.

Option B:
- Add “Report Concern” as a clear card on the home/safety screen.

Do not place important actions in floating buttons that cover forms or content.

---

## 4. Simplify Navigation Labels

Rename bottom navigation labels to be more human and less technical.

Use:

| Current Label | New Label  |
| ------------- | ---------- |
| Mediation     | Resolve    |
| Vault         | Agreements |
| Safety        | Alerts     |
| Network       | Sync       |

Keep icons, but ensure icons are simple and recognizable.

Each navigation item must have both:
- icon,
- visible text label.

---

## 5. Use Clear Human Language

Replace technical wording with simple language.

Use these replacements:

| Current Text                  | Better Text                            |
| ----------------------------- | -------------------------------------- |
| BLE/Wi-Fi Direct peer scan    | Nearby device connection               |
| Sync Queue                    | Waiting to share safely                |
| Items Waiting For Relay       | Items waiting to share                 |
| Local encrypted vault         | Private agreements saved on this phone |
| BIP-39 recovery phrase        | Recovery words                         |
| Generate Neutralized Guidance | Help me respond calmly                 |
| Conflict description          | What happened?                         |
| General area                  | Approximate area                       |
| Observed risk                 | What risk did you notice?              |
| Local only                    | Saved only on this phone               |

Avoid jargon such as:
- BIP-39,
- libp2p,
- gossiping,
- peer relay,
- zero knowledge,
- cryptographic,
- protocol.

Only expose technical detail in developer/debug screens, not user-facing screens.

---

## 6. Replace Form-First Screens With Guided Flows

Distressed users should not face large empty text fields first.

Use guided step-by-step flows.

### Resolve Flow

Step 1: Choose conflict type:
- Water
- Land
- Grazing
- Family
- Neighbor
- Other

Step 2: What happened?
Offer simple options first:
- Access was blocked
- Threats were made
- Property was damaged
- Agreement was broken
- I am not sure

Step 3: What help do you need?
- Help me calm the situation
- Help me talk to them
- Help create an agreement
- Help find next safe step

Step 4:
Show AI-generated peaceful guidance.

### Alerts Flow

Step 1: What type of concern?
- Threat
- Rumor
- Movement / displacement
- Resource conflict
- Other

Step 2: Approximate area only.
Do not ask for exact GPS by default.

Step 3: What did you observe?

Step 4: Local verification reminder:
- “Only share what you saw or trust.”
- “Mark uncertain reports as unverified.”

---

## 7. Add Calm Mode

Add a reusable “Calm Mode” component before AI guidance.

When a user describes a tense situation, show:

> Take a moment. Let’s work through this safely and calmly.

Then offer:
- “Help me respond calmly”
- “Create a neutral summary”
- “Suggest next steps”

The tone should be:
- calm,
- non-judgmental,
- de-escalating,
- safety-aware.

Do not encourage confrontation.

---

## 8. Add Safe Exit

Add a visible but unobtrusive “Quick Exit” option.

Purpose:
- allow a user to leave the screen quickly if they feel unsafe.

Possible behavior for MVP:
- return to a neutral screen,
- hide sensitive content,
- open a harmless-looking page,
- or close sensitive flows.

Label examples:
- “Quick Exit”
- “Hide Screen”

Keep the action easy to access but avoid making it visually alarming.

---

## 9. Improve Offline Status Communication

Users should always understand whether the app is usable offline.

Add a small status banner near the top:

Examples:
- “Offline ready”
- “Saved on this phone”
- “Waiting to share safely”
- “Connected — syncing now”

Use icons:
- check icon for ready,
- clock icon for waiting,
- sync icon for syncing.

Do not use technical network language.

---

## 10. Reduce Empty Space and Improve Small-Screen Fit

The current design has large empty areas that may cause unnecessary scrolling.

Improve layout by:
- reducing excessive vertical whitespace,
- grouping related actions into cards,
- keeping key actions visible above the fold,
- ensuring forms and buttons do not get covered by navigation or overlays.

Support small screens by:
- using `LazyColumn`,
- using adaptive padding,
- avoiding fixed heights where possible,
- making long chips horizontally scrollable only when necessary.

---

## 11. Accessibility Requirements

Implement accessibility-friendly UI behavior:

- Minimum touch target: `48.dp`
- Prefer `56.dp` or larger for main actions
- Support large font scaling
- Use semantic labels for icons
- Do not use color alone to show meaning
- Provide clear focus states
- Avoid tiny icon-only buttons for core actions
- Use high contrast for text and buttons
- Ensure bottom navigation remains readable

Add content descriptions for icons in Compose.

---

## 12. Performance Requirements

The app must stay lightweight for low-end Android phones.

Avoid:
- heavy animations,
- Lottie animations,
- large images,
- complex gradients,
- blur effects,
- unnecessary recompositions,
- network-dependent startup.

Prefer:
- simple vector icons,
- solid colors,
- local-first state,
- lightweight Compose components,
- Room/SQLite for local storage,
- lazy lists,
- minimal dependencies.

---

## 13. Screen-Specific Improvements

### Resolve Screen

Current issue:
- too form-like,
- large blank input,
- technical button wording.

Improve by:
- renaming “Mediation” to “Resolve”,
- replacing the first screen with conflict type cards,
- using guided steps,
- changing button text to “Help me respond calmly”,
- showing a calm reassurance card before input.

### Agreements Screen

Current issue:
- “Vault” sounds technical,
- BIP-39 wording is too technical.

Improve by:
- renaming “Vault” to “Agreements”,
- replacing “Local encrypted vault. BIP-39 recovery phrase ready for future auth.” with:
  “Private agreements are saved on this phone. Recovery words can help restore access later.”
- showing agreement status clearly:
  - Draft
  - Agreed
  - Waiting to share
  - Shared

### Alerts Screen

Current issue:
- “Safety” is okay but “Alerts” is clearer for reporting.
- map/radius card should emphasize approximate location.

Improve by:
- renaming “Safety” to “Alerts”,
- keeping approximate location only,
- making the report flow step-based,
- adding verification reminder:
  “Share only what you observed or trust. Mark rumors as unverified.”

### Sync Screen

Current issue:
- “Network” and BLE/Wi-Fi wording are technical.

Improve by:
- renaming “Network” to “Sync”,
- replacing description with:
  “Share saved items with nearby trusted devices when possible.”
- replacing “Scan” with “Find nearby devices”
- replacing “0 Neighbors Nearby” with “0 nearby devices found”
- replacing “Sync Queue” with “Waiting to share safely”

---

## 14. Agreement and Anonymity UX

Agreement drafts must preserve anonymity by default.

Use labels:
- Party A
- Party B
- Community Representative
- Witness / Mediator

Do not require:
- names,
- phone numbers,
- exact locations,
- government IDs.

Allow users to:
- save an anonymous draft,
- copy it,
- share it offline,
- print it later,
- formalize it outside the app with trusted elders, mediators, chiefs, or community leaders.

Use wording:

> This draft does not need names inside the app. You can add names later offline if both sides agree.

---

## 15. Final Product Feel

The final app should feel:
- calm,
- simple,
- readable,
- trustworthy,
- community-centered,
- safe,
- accessible to elderly users,
- usable on low-end Android phones,
- usable during stress.

Avoid making the UI feel:
- corporate,
- overly technical,
- futuristic,
- surveillance-like,
- police-like,
- or data-heavy.

---

## Codex Task

Refactor the existing Kotlin + Jetpack Compose UI according to the guidelines above.

Start with:
1. Rename navigation labels.
2. Remove the floating Report button.
3. Increase typography and contrast.
4. Convert Resolve and Alerts into guided flows.
5. Add Offline Status banner.
6. Add Safe Exit action.
7. Replace technical wording with human language.
8. Ensure components remain lightweight and accessible.

Preserve the existing app structure where possible, but prioritize usability and accessibility for distressed and elderly users.
