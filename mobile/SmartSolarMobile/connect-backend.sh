#!/bin/sh
set -eu

if command -v adb >/dev/null 2>&1; then
    solar_adb=$(command -v adb)
else
    solar_sdk=${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}}
    solar_adb="$solar_sdk/platform-tools/adb"
fi

if [ ! -x "$solar_adb" ]; then
    echo 'ADB was not found. Add platform-tools to PATH or set ANDROID_HOME.' >&2
    exit 1
fi

if [ "$#" -gt 0 ]; then
    solar_devices=$1
else
    solar_devices=$("$solar_adb" devices | awk 'NR > 1 && $2 == "device" { print $1 }')
fi

if [ -z "$solar_devices" ]; then
    echo 'Connect and authorize your phone for USB debugging, or start an emulator.' >&2
    exit 1
fi

for solar_serial in $solar_devices; do
    "$solar_adb" -s "$solar_serial" reverse tcp:5103 tcp:5103
    echo "Backend forwarding ready for $solar_serial (port 5103)."
done

echo 'Keep the backend running on localhost:5103 and keep the device connected.'
