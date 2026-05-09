Role: You are a Senior Android UI/UX Engineer specializing in Jetpack Compose, Material 3, and Security-First application design.

Objective: Build a performant, secure, and offline-resilient UI for the Jirani PeaceTech App. The design must strictly adhere to the "Grounded Resilience" system (Primary Green: #2E7D32, Surface Sand: #F9FBE7).

1. Global Framework & Navigation
Theme: Implement a MaterialTheme using lightColorScheme with high-contrast accessibility. Use Roboto Flex for dynamic typography.

Scaffold:

Top Bar: Implement a CenterAlignedTopAppBar.

Panic Button (Left): An icon that, when tapped, instantly navigates to a DecoyScreen (a functional calculator) and clears the NavController backstack to prevent data exposure.

Mesh Status (Right): A pulsing Icon that changes from Gray to Primary Green when a peer is detected via BLE or Wi-Fi Direct.

Bottom Navigation: Provide a NavigationBar with labels for Mediation, Vault, Safety, and Network.

2. Feature Screen Specifications
Mediation Hub: * Implement a Chat-style UI.

Feature a reactive "Tone Check" Card that suggests a "Neutralized" version of user input to aid de-escalation.

Use a scrollable LazyRow of Action Chips (e.g., "Water Access," "Grazing Rights").

Vault Screen: * A list of ElevatedCard items representing agreements.

Each card must display a Sync Status Badge (Local, Mesh, or Cloud) and an encryption lock icon.

Safety/Reporting Screen: * A stepper form for anonymous threat reporting (e.g., rustling, robbery).

Include a map view with Geospatial Obfuscation (a fuzzy radius instead of a precise pin) to confirm user anonymity.

Network Screen: * A radial pulse animation indicating a background scan for peers.

Display stats for "Nearby Neighbors" and "Queue Size".

3. Performance & Hardware Guardrails
Low-End Optimization: Avoid Gaussian blurs or heavy drop shadows. Use outlines and surface color shifts to indicate state.

State Management: Use ViewModel with StateFlow. Collect states in the UI using collectAsStateWithLifecycle.

Anonymity: Ensure no PII (Phone/Email) is collected. All authentication uses BIP-39 mnemonic recovery phrases.

Assets: Use 100% VectorDrawables for icons to keep the APK size minimal.

4. Local-First Logic
All UI interactions must prioritize local Room database updates immediately, with synchronization occurring as a background side effect.