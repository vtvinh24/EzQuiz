#!/bin/bash
# Script to package and sign Chrome extension as .crx (CRX2 format)
set -e

EXT_NAME="EzQuiz-ext"
ZIP_NAME="${EXT_NAME}.zip"
CRX_NAME="${EXT_NAME}.crx"
PRIV_KEY="$(dirname "$0")/EzQuiz-ext.pem"
PUB_KEY="$(dirname "$0")/public_key.pem"

# Go to project root
cd "$(dirname "$0")/.."

# Remove old files if exist
rm -f "$ZIP_NAME" "$CRX_NAME" signature.der pubkey.der

# Create zip, excluding specified files/folders
zip -r "$ZIP_NAME" . \
  -x "*.pem" \
  -x "tools/*" \
  -x "dist/*" \
  -x ".gitignore"

# Generate signature in DER format
openssl sha1 -binary -sign "$PRIV_KEY" "$ZIP_NAME" > signature.der

# Convert public key to DER format
openssl rsa -pubout -outform DER -in "$PRIV_KEY" > pubkey.der

# Build CRX2 header
# Magic number (Cr24), version (2), pubkey length, sig length
MAGIC=$(echo -n 'Cr24')
VERSION=$(printf '\x02\x00\x00\x00')
PUBKEY_LEN=$(printf "%08x" $(stat -c%s pubkey.der) | sed 's/../\\x&/g')
SIG_LEN=$(printf "%08x" $(stat -c%s signature.der) | sed 's/../\\x&/g')

# Concatenate all parts
{
  echo -ne "$MAGIC$VERSION$PUBKEY_LEN$SIG_LEN"
  cat pubkey.der
  cat signature.der
  cat "$ZIP_NAME"
} > "$CRX_NAME"

# Clean up
rm -f signature.der pubkey.der "$ZIP_NAME"

echo "Created $CRX_NAME"
