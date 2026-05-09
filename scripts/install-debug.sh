#!/usr/bin/env bash
set -euo pipefail

# scripts/install-debug.sh
# Assembles the debug APK and installs it to a connected device.
# Usage:
#   ./scripts/install-debug.sh                    # installs to first connected device
#   ./scripts/install-debug.sh -d <deviceId>      # installs to the specified device
#   ./scripts/install-debug.sh -u                  # uninstall existing app before installing
#   ./scripts/install-debug.sh -d <deviceId> -u -r 5  # custom retries

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
APK_PATH="$PROJECT_ROOT/app/build/outputs/apk/debug/app-debug.apk"
PACKAGE_NAME="com.jirani.app"

# Defaults
DEVICE_ID=""
UNINSTALL_FIRST=false
RETRIES=3

usage() {
  cat <<EOF
Usage: $0 [-d deviceId] [-u] [-r retries]
  -d, --device     Install to specific device id (from adb devices)
  -u, --uninstall  Uninstall existing app before installing
  -r, --retries    Number of adb install retries (default: 3)
  -h, --help       Show this help

Examples:
  $0
  $0 -d aaed2db57d77
  $0 -u
  $0 -d aaed2db57d77 -u -r 5
EOF
}

# Parse args
while [[ $# -gt 0 ]]; do
  case "$1" in
    -d|--device)
      DEVICE_ID="$2"; shift 2;;
    -u|--uninstall)
      UNINSTALL_FIRST=true; shift 1;;
    -r|--retries)
      RETRIES="$2"; shift 2;;
    -h|--help)
      usage; exit 0;;
    *)
      echo "Unknown arg: $1" >&2; usage; exit 1;;
  esac
done

echo "Building debug APK..."
./gradlew assembleDebug

if [[ "$UNINSTALL_FIRST" == true ]]; then
  echo "Uninstalling existing app..."
  adb ${DEVICE_ID:+-s $DEVICE_ID} uninstall "$PACKAGE_NAME" || true
fi

echo "Installing debug APK to device..."
for i in $(seq 1 "$RETRIES"); do
  if adb ${DEVICE_ID:+-s $DEVICE_ID} install -r "$APK_PATH"; then
    echo "Installation successful."
    exit 0
  else
    echo "Installation attempt $i failed. Retrying..."
    sleep 2
  fi
done

echo "Installation failed after $RETRIES attempts."
exit 1