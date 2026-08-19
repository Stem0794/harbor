#!/bin/sh
set -eu

apk_path=${1:?Usage: verify-release-signing.sh path-to-signed-apk}
apksigner_bin=${APKSIGNER_BIN:-apksigner}
expected_sha256=${HARBOR_ALLOWED_SIGNING_SHA256:-1f68efbefd07ea0c1aa7d79d9fd720c3dda74ac5524dcf398efea0d379b3494d}
expected_sha256=$(printf '%s' "$expected_sha256" | tr '[:upper:]' '[:lower:]' | tr -d ':')

if [ -x "$apksigner_bin" ]; then
  :
elif command -v "$apksigner_bin" >/dev/null 2>&1; then
  :
else
  printf 'apksigner not found: %s\n' "$apksigner_bin" >&2
  printf 'Set APKSIGNER_BIN to the intended Android Build Tools 34 apksigner path.\n' >&2
  exit 1
fi

cert_output=$("$apksigner_bin" verify --print-certs "$apk_path")
digests=$(printf '%s\n' "$cert_output" | sed -n 's/^Signer #[0-9][0-9]* certificate SHA-256 digest: //p' | tr '[:upper:]' '[:lower:]' | tr -d ':')
signer_count=$(printf '%s\n' "$digests" | sed '/^$/d' | wc -l | tr -d ' ')

if [ "$signer_count" -ne 1 ]; then
  printf 'Expected exactly one APK signer, found %s.\n' "$signer_count" >&2
  exit 1
fi

actual_sha256=$(printf '%s\n' "$digests" | sed -n '1p')
if [ "$actual_sha256" != "$expected_sha256" ]; then
  printf 'Unexpected APK signing certificate.\n' >&2
  printf 'Expected: %s\n' "$expected_sha256" >&2
  printf 'Actual:   %s\n' "$actual_sha256" >&2
  exit 1
fi

printf 'Release signing certificate verified: %s\n' "$actual_sha256"
