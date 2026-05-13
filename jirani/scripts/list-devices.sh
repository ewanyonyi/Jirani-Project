#!/usr/bin/env bash
set -euo pipefail

# scripts/list-devices.sh
# Lists connected Android devices and available virtual devices.

echo "Connected Android devices:"
adb devices

echo ""
echo "Available Android Virtual Devices:"
emulator -list-avds
