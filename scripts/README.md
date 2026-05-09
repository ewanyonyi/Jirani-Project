# Scripts

This folder contains helper scripts for local Android development and debugging.

## Available scripts

### `install-debug.sh`
Builds the debug APK and installs it to a connected device.

Usage:

- `./scripts/install-debug.sh`
- `./scripts/install-debug.sh -d <deviceId>`
- `./scripts/install-debug.sh -u`
- `./scripts/install-debug.sh -d <deviceId> -u -r 5`

The script supports:

- selecting a specific device via `-d`
- uninstalling the existing package first via `-u`
- retrying install attempts via `-r`

### `build-release-aab.sh`
Builds a release Android App Bundle and optionally sets up signing properties.

Usage:

- `./scripts/build-release-aab.sh`
- `./scripts/build-release-aab.sh --keystore-file release-keystore.jks --store-password <pass> --key-password <pass> --key-alias <alias>`
- `./scripts/build-release-aab.sh --keystore-base64-file keystore.jks.base64 --store-password <pass> --key-password <pass> --key-alias <alias>`

The script can decode a base64-encoded keystore, write `keystore.properties`, and run `./gradlew bundleRelease`.

### `list-devices.sh`
Lists connected Android devices and available Android Virtual Devices.

Usage:

- `./scripts/list-devices.sh`

This runs:

- `adb devices`
- `emulator -list-avds`

### `monitor-logs.sh`
Monitors Android logcat output for the Jirani app and crash-related logs.

Usage:

- `./scripts/monitor-logs.sh`

The script waits for a device, clears previous logs, and starts logcat with filters for:

- `com.jirani.app`
- `AndroidRuntime`
- `ActivityManager`
