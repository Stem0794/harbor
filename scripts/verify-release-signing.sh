#!/bin/sh
set -eu

apk_path=${1:?Usage: verify-release-signing.sh path-to-signed-apk}
expected_sha256=${HARBOR_ALLOWED_SIGNING_SHA256:-1f68efbefd07ea0c1aa7d79d9fd720c3dda74ac5524dcf398efea0d379b3494d}
expected_sha256=$(printf '%s' "$expected_sha256" | tr '[:upper:]' '[:lower:]' | tr -d ':')

resolve_apksigner() {
  if [ -n "${APKSIGNER_BIN:-}" ]; then
    printf '%s\n' "$APKSIGNER_BIN"
    return
  fi

  if [ -n "${ANDROID_SDK_ROOT:-}" ] && [ -x "$ANDROID_SDK_ROOT/build-tools/34.0.0/apksigner" ]; then
    printf '%s\n' "$ANDROID_SDK_ROOT/build-tools/34.0.0/apksigner"
    return
  fi

  if [ -n "${ANDROID_HOME:-}" ] && [ -x "$ANDROID_HOME/build-tools/34.0.0/apksigner" ]; then
    printf '%s\n' "$ANDROID_HOME/build-tools/34.0.0/apksigner"
    return
  fi

  printf 'Android Build Tools 34 apksigner not found.\n' >&2
  printf 'Install build-tools;34.0.0 or set APKSIGNER_BIN explicitly.\n' >&2
  exit 1
}

apksigner_bin=$(resolve_apksigner)
if [ ! -x "$apksigner_bin" ]; then
  printf 'apksigner is not executable: %s\n' "$apksigner_bin" >&2
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
