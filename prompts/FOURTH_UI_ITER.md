# Jirani UI Iteration 4: AI-Guided Chat & Interaction Design

## 1. Interaction Strategy: The "Human-Centric Thread"
This iteration moves from form-based inputs to a collaborative chat-style interface. The goal is to make the AI feel like a "Mediator in the Room" rather than a database entry tool [cite: 2.5, 4.1].

## 2. Screen 1: The Mediation Hub (Refined Chat)
- **Top Bar:** Features a "Disguise" (Calculator) icon on the left and a "Mesh Health" indicator on the right [cite: 2.5, 3.3].
- **Message Bubbles:**
    - **User Input (Right):** Green bubbles with high-contrast text.
    - **Jirani Assistant (Left):** Soft sand-colored bubbles with Roboto Flex typography [cite: 2.5].
- **The Neutralizer Overlay:** When a user types a high-emotion sentence, a "Tone Check" card slides in above the keyboard offering a "Safe Version" to send instead [cite: 2.5, 3.3].
- **Action Chips:** Horizontal scrolling chips for "Water Access," "Grazing Rights," and "Land Dispute" to minimize typing effort [cite: 3.3].

## 3. Screen 2: The Vault (Library View)
- **Search & Filter:** Local-only search bar at the top with chips for "Signed" and "Draft" [cite: 2.3].
- **Record Cards:**
    - **Visual Lock:** Every card displays a small lock icon to visually confirm local disk encryption [cite: 2.3].
    - **Sync Badge:** A custom icon set showing "Local Only" (Amber clock), "Mesh Synced" (Gossip dots), or "Gateway Uploaded" (Green check) [cite: 4.1].

## 4. Screen 3: Safety & Anonymous Reporting
- **Stepper Progress:** A visual track (1. Details -> 2. Region -> 3. Local Verification) to reduce stress [cite: 3.1].
- **Fuzzy Mapping:** A map component displaying a "Noisy/Grainy" circle to represent the report area without revealing the reporter's exact GPS coordinates [cite: 2.5, 3.1].
- **One-Tap Reporting:** Large visual icons for "Rustling," "Threat," or "Rumor" [cite: 3.1].

## 5. Screen 4: Network (The Heartbeat)
- **The Pulse:** A central "Scan" button with a low-power ripple effect.
- **Neighbor Counter:** "5 Neighbors Nearby" text—providing social proof of the network without exposing individual IDs [cite: 4.1].
- **Sync Queue:** A vertical progress list showing "3 items waiting for relay" [cite: 4.1].

## 6. Technical Implementation Guidelines for AI Agents
- **Compose Patterns:** Use `LazyColumn` for the chat thread and `Surface` with `1dp` stroke for cards to save GPU cycles [cite: 2.4].
- **State Management:** Use `collectAsStateWithLifecycle()` to bind Room database flows to the UI [cite: 2.3].
- **Assets:** Use Vector Drawables for all icons to keep the APK lightweight for older Android devices [cite: 2.4].
