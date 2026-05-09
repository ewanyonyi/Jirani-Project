# Jirani Design System & UI Specification

## 1. Visual Identity: "Grounded Resilience"
Designed for high-stakes PeaceTech utility in the Somali-Kamba border context and globally. Optimized for high-sunlight visibility and low-end Android hardware.

### Color Palette (Material 3 Implementation)
- **Primary:** `#2E7D32` (Forest Green) - Peace and stability.
- **OnPrimary:** `#FFFFFF`
- **Secondary:** `#795548` (Terra Cotta) - Community and earth.
- **Surface:** `#F9FBE7` (Soft Sand) - High readability, low eye strain.
- **OnSurface:** `#1B1C17`
- **Error/Alert:** `#BF360C` (Deep Burnt Orange) - Serious but non-alarmist.

### Typography
- **Primary Font:** `Roboto Flex` (Variable weights for hierarchy).
- **Scale:**
  - Headline Large: 32sp (Mediation headers)
  - Body Large: 18sp (Primary chat and agreement text)
  - Label Medium: 12sp (Metadata and sync status)

## 2. Core UI Architecture (Jetpack Compose)

### Navigation (BottomBar)
1. **Mediation (Home):** AI chat interface for de-escalation.
2. **Vault (Agreements):** Local library of signed records.
3. **Safety (Alerts):** Anonymous threat reporting map/list.
4. **Network (Sync):** Mesh status and P2P radar.

### Key Components
- **MediationCard:** Bubble-style UI with "Neutralizer" suggestions.
- **AgreementItem:** Elevated card with status icons (Verified, Pending Sync).
- **GhostSyncRadar:** Pulse animation showing nearby BLE/Wi-Fi Direct peers.
- **PanicTrigger:** Floating Action Button (FAB) or gesture for immediate app disguise.

## 3. Screen Specifications

### Mediation Screen
- **Input:** Multi-line text field with real-time AI "Tone Check."
- **AI Feedback:** A "Safe Version" suggestion box appearing above the keyboard.
- **Action Chips:** "Water Rights", "Border Crossing", "Grazing Access" for rapid input.

### Vault Screen
- **Search:** Local-only search bar.
- **Filter:** Status chips (Signed, Draft, Synced).
- **Encryption Badge:** Permanent visual indicator of local-disk encryption status.

### Safety Reporting Screen
- **Icon Grid:** Large, high-contrast icons for threat categories.
- **Fuzzy Location:** Visual "Radius Circle" map to confirm obfuscation to the user.
- **Submission:** One-tap anonymous send with auto-deletion of local drafts.

## 4. Technical Constraints Implementation
- **Performance:** Avoid `Shadows` and `Blurs`; use `Outline` and `Color` for elevation to save GPU cycles.
- **Battery:** Use `SideEffects` to manage BLE scanning intervals (Pulse Mode).
- **Icons:** Strict use of **Vector Drawables (SVG)** to minimize APK size.

## 5. Coding Agent Instructions
- **Framework:** Jetpack Compose (Kotlin).
- **Theme:** Use `MaterialTheme` with custom `ColorScheme` defined above.
- **State:** Use `collectAsStateWithLifecycle()` for all Room/Sync streams.
- **Privacy:** Ensure no ViewModels or UI logs ever print sensitive user data to Logcat.
