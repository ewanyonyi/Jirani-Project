# Jirani App

An offline-first PeaceTech Android app for careful conflict reporting, local verification, elder-led mediation, agreement records, and resilient community coordination.

## Features
- Conflict, threat, domestic violence, and GBV reports before mediation
- Local triage for protection, verification, or elder review
- Survivor-centered handling that does not broadcast domestic violence or GBV reports
- Mediation guidance only after trusted actors decide people can meet safely
- Neutral agreement summary foundation
- Offline-first design
- Kotlin + Jetpack Compose Android UI

## Getting Started
1. Clone the repository
2. Open in Android Studio
3. Build and run

## Architecture
Offline-first with local data storage, privacy-first records, and future peer-to-peer sync capabilities.

## Optional Rust Gateway

The companion Rust/Rocket test gateway lives at `/home/ewanyonyi/dev/jirani-rust`.

Android uses it only as an optional minimized-envelope gateway. Reports still work locally and through Nearby without the server. To point Android at a hosted test server, build with:

```bash
./gradlew assembleDebug \
  -PJIRANI_REMOTE_GATEWAY_URL=https://snf-6731.vlab.ac.ke \
  -PJIRANI_REMOTE_GATEWAY_TOKEN=change-this-demo-token
```

For local gateway testing, run:

```bash
cd /home/ewanyonyi/dev/jirani-rust
cargo run
```

For hosted testing, use HTTPS and a gateway token. Android will not use plain
HTTP for non-local gateway hosts. See `docs/COMMUNICATION.md` for the full
Android <-> Rust sync and relay contract.

Current hosted test gateway:

```text
https://snf-6731.vlab.ac.ke
```

Install on a connected physical device with the hosted gateway config:

```bash
./gradlew installDebug \
  -PJIRANI_REMOTE_GATEWAY_URL=https://snf-6731.vlab.ac.ke \
  -PJIRANI_REMOTE_GATEWAY_TOKEN=change-this-demo-token
```

Or use environment variables:

```bash
JIRANI_REMOTE_GATEWAY_URL=https://snf-6731.vlab.ac.ke \
JIRANI_REMOTE_GATEWAY_TOKEN=change-this-demo-token \
./gradlew installDebug
```

## Nearby Connections Sync

Jirani uses **Google Nearby Connections** as the first real device-to-device transport for report movement, matching the `CAPSTONE_SPEC.md` direction for trusted nearby sharing. The goal is to let two phones running Jirani find each other without internet, then use the existing sanitized sync-envelope flow to move eligible reports.

Current implementation:

- `app/src/main/java/com/jirani/app/sync/NearbyConnectionsScanner.kt`
  - starts Nearby advertising so this phone can be found by another Jirani phone;
  - starts Nearby discovery so this phone can find nearby Jirani phones;
  - requests a Nearby connection when another Jirani phone is found;
  - accepts incoming Jirani connection requests;
  - sends and receives anonymized report packets as Nearby byte payloads;
  - uses the service ID `com.jirani.app.nearby`;
  - uses `Strategy.P2P_CLUSTER` so multiple nearby trusted phones can participate;
  - publishes discovered phones as `NearbyJiraniDevice` records using `SyncTransport.NearbyConnections`.
- `app/src/main/java/com/jirani/app/ui/sync/SyncScreen.kt`
  - requests runtime Nearby permissions;
  - shows the shared app-level Nearby sync status;
  - duty-cycles active discovery to reduce battery use;
  - keeps existing Nearby connections alive when discovery is paused;
  - shows how many nearby Jirani devices were found;
  - automatically sends waiting eligible reports after a nearby device is connected;
  - keeps submitted report delivery counts visible.
- `app/src/main/java/com/jirani/app/sync/NearbySyncRuntime.kt`
  - owns the app-level Nearby scanner while Jirani is open;
  - starts availability when permissions are granted;
  - starts discovery immediately after an eligible report is submitted;
  - sends queued reports without requiring the user to open the Sync screen.
- `app/src/main/java/com/jirani/app/data/local/LocalFirstUiStore.kt`
  - receives discovered devices;
  - updates the sync state;
  - attempts queued report sharing when eligible devices are found.
  - persists pending, submitted, and received reports in app-private storage so they survive app and phone restart.

Important privacy rule: Nearby scanning only finds other Jirani devices. Report content still goes through the existing sanitized sync-envelope policy. Domestic violence and GBV reports remain local by default and are not broadcast through nearby community sharing.

### Permissions

Nearby Connections uses Bluetooth, Wi-Fi, and nearby-device radios under the hood. The app declares these permissions in `app/src/main/AndroidManifest.xml`:

| Permission | Why Jirani needs it |
|---|---|
| `ACCESS_WIFI_STATE` | Required by Nearby Connections to inspect Wi-Fi radio state before discovery/advertising. |
| `CHANGE_WIFI_STATE` | Required by Nearby Connections for local Wi-Fi-based connection setup. |
| `BLUETOOTH` and `BLUETOOTH_ADMIN` | Required on Android 11/API 30 and below for Bluetooth discovery support. |
| `ACCESS_COARSE_LOCATION` | Required on older Android versions where Bluetooth/Wi-Fi discovery is treated as location-adjacent. |
| `ACCESS_FINE_LOCATION` | Required on Android 10-12/API 29-31 for Nearby Connections discovery. |
| `BLUETOOTH_SCAN` | Required on Android 12/API 31 and newer to scan for nearby Bluetooth devices. |
| `BLUETOOTH_CONNECT` | Required on Android 12/API 31 and newer to connect to nearby Bluetooth devices. |
| `BLUETOOTH_ADVERTISE` | Required on Android 12/API 31 and newer so this phone can advertise itself as a Jirani device. |
| `NEARBY_WIFI_DEVICES` | Required on Android 13/API 33 and newer for nearby Wi-Fi device discovery. |

`BLUETOOTH_SCAN` and `NEARBY_WIFI_DEVICES` use `android:usesPermissionFlags="neverForLocation"` because Jirani does not use nearby scanning to infer or store a user's location. The app also avoids exact GPS, phone numbers, personal names, and device identifiers in report payloads.

Runtime permission handling is in `SyncScreen.kt`. If the user denies the required permissions, Nearby scanning will not start.

### App Settings

The Settings screen includes:

- **Nearby sharing:** app-level toggle for Nearby discovery, advertising, and report sending. Turning it off keeps reports queued locally. Android OS permissions are still managed by Android system settings.
- **Active relay mode:** explicit foreground-service mode for keeping relay availability on with a visible notification.
- **Language:** current app preference for English, Swahili, Somali, or Kamba. The wider translation work is still a future phase.
- **Theme:** Light or Dark display mode.
- **Calculator decoy code:** local code used to return from the decoy screen.

### Battery Behavior

Jirani separates **availability** from **active discovery**:

- Availability/advertising stays on while the Sync screen is active so another nearby Jirani phone can find this phone.
- Active discovery runs only when there is an eligible unshared report waiting, including immediately after report submission.
- Discovery runs in 45-second bursts, then rests for 30 seconds if no connected device is found.
- When there are no waiting reports or relay bundles, discovery pauses automatically.
- Existing Nearby connections remain open when discovery pauses, so a connected phone can still receive data.

Active relay mode uses a foreground service with a visible notification. Production deployments should still tune stricter duty cycling for local battery constraints.

### Testing Nearby Discovery

Use two Android phones for the real test. Emulators are not reliable for Nearby Connections radio behavior.

1. Build and install the app on both phones.
2. Open Jirani on both phones.
3. Go to `Sync`.
4. Grant the requested Bluetooth, nearby device, Wi-Fi, or location permissions if prompted.
5. Keep both phones close together.
6. The Sync screen should become available automatically.
7. The Sync screen should show found devices first, then connected devices after the Nearby handshake completes.
8. Submit a non-GBV/non-domestic report on one phone.
9. If a connected device is available, Jirani should send the waiting eligible report automatically, even before opening Sync. If no device is connected, discovery will scan in battery-aware bursts.
10. Open Sync to confirm the sending phone updated `Sent to X/5 devices`; the receiving phone should show the anonymized report in the receiving-device inbox.

If you see a Nearby error such as `8032: MISSING_PERMISSION_ACCESS_WIFI_STATE` or `8033: MISSING_PERMISSION_CHANGE_WIFI_STATE`, reinstall the app after pulling the latest manifest changes. Android must receive the updated permission declarations from a fresh install or upgrade.

### Referenced Docs

- Google Nearby Connections get started: https://developers.google.cn/nearby/connections/android/get-started
- Google Nearby Connections advertise and discover: https://developers.google.cn/nearby/connections/android/discover-devices
- Jirani capstone sync direction: `CAPSTONE_SPEC.md`
- Jirani information flow and privacy rules: `docs/INFORMATION_FLOW.md`

See `AGENTS.md`, `PLAN.md`, `CAPSTONE_SPEC.md`, `docs/REAL_WORLD_CONFLICT_RESOLUTION.md`, and `docs/` for the Codex-first development workflow.
