#!/usr/bin/env bash
set -euo pipefail

# scripts/build-release-aab.sh
# Local helper to decode a base64-encoded keystore file (or use an existing keystore),
# create keystore.properties, and run bundleRelease.

usage() {
  cat <<EOF
Usage: $0 [--keystore-base64-file FILE] [--keystore-file FILE] [--store-password PASS] [--key-password PASS] [--key-alias ALIAS]

Options:
  --keystore-base64-file FILE  File containing base64-encoded keystore (one-line). If provided, it will be decoded to release-keystore.jks
  --keystore-file FILE         Path to an existing keystore (e.g., release-keystore.jks)
  --store-password PASS        Keystore password (required unless keystore properties already exist)
  --key-password PASS          Key password (required unless keystore properties already exist)
  --key-alias ALIAS            Key alias (default: jirani)
  -h, --help                   Show this help

Examples:
  # Decode base64 keystore and build
  ./scripts/build-release-aab.sh --keystore-base64-file keystore.jks.base64 --store-password "pass" --key-password "pass" --key-alias jirani

  # Use an existing keystore file
  ./scripts/build-release-aab.sh --keystore-file release-keystore.jks --store-password "pass" --key-password "pass" --key-alias jirani
EOF
}

KESTORE_BASE64_FILE=""
KEYSTORE_FILE=""
STORE_PASSWORD=""
KEY_PASSWORD=""
KEY_ALIAS="jirani"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --keystore-base64-file)
      KESTORE_BASE64_FILE="$2"; shift 2;;
    --keystore-file)
      KEYSTORE_FILE="$2"; shift 2;;
    --store-password)
      STORE_PASSWORD="$2"; shift 2;;
    --key-password)
      KEY_PASSWORD="$2"; shift 2;;
    --key-alias)
      KEY_ALIAS="$2"; shift 2;;
    -h|--help)
      usage; exit 0;;
    *)
      echo "Unknown arg: $1"; usage; exit 1;;
  esac
done

# If keystore base64 file is provided, decode it
if [[ -n "$KESTORE_BASE64_FILE" ]]; then
  echo "Decoding keystore from $KESTORE_BASE64_FILE..."
  base64 -d "$KESTORE_BASE64_FILE" > release-keystore.jks
  KEYSTORE_FILE="release-keystore.jks"
fi

# Create keystore.properties if passwords are provided
if [[ -n "$STORE_PASSWORD" && -n "$KEY_PASSWORD" && -n "$KEYSTORE_FILE" ]]; then
  echo "Creating keystore.properties..."
  cat > keystore.properties <<EOF
storePassword=$STORE_PASSWORD
keyPassword=$KEY_PASSWORD
keyAlias=$KEY_ALIAS
storeFile=$KEYSTORE_FILE
EOF
fi

# Build the release AAB
echo "Building release AAB..."
./gradlew bundleRelease

echo "Release AAB built successfully."