#!/bin/bash
echo "Waiting for device..."
adb wait-for-device
echo "Device connected. Clearing previous logs..."
adb logcat -c
echo "Monitoring logs for Jirani app and Crashes..."
echo "------------------------------------------------"
# Filter for:
# - com.jirani.app (Debug/Info)
# - AndroidRuntime (Errors - for crashes)
# - ActivityManager (Info - for process start/death)
adb logcat -v time com.jirani.app:D AndroidRuntime:E ActivityManager:I *:S