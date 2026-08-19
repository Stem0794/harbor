#!/bin/sh
set -eu

unsigned_apk=${1:-app/build/outputs/apk/release/app-release-unsigned.apk}
signed_apk=${2:-app-release-signed.apk}

: "${HARBOR_RELEASE_KEYSTORE:?Set HARBOR_RELEASE_KEYSTORE to the private release keystore path.}"
: "${HARBOR_RELEASE_KEY_ALIAS:?Set HARBOR_RELEASE_KEY_ALIAS to the release key alias.}"
: "${HARBOR_KEYSTORE_PASSWORD:?Set HARBOR_KEYSTORE_PASSWORD in the environment.}"
: "${HARBOR_KEY_PASSWORD:?Set HARBOR_KEY_PASSWORD in the environment.}"

if [ ! -f "$unsigned_apk" ]; then
  printf 'Unsigned release APK not found: %s\n' "$unsigned_apk" >&2
  exit 1
fi

if [ ! -f "$HARBOR_RELEASE_KEYSTORE" ]; then
  printf 'Release keystore not found: %s\n' "$HARBOR_RELEASE_KEYSTORE" >&2
  exit 1
fi

if [ "$unsigned_apk" = "$signed_apk" ]; then
  printf 'Input and output APK paths must be different.\n' >&2
  exit 1
fi

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

cleanup=1
trap 'if [ "$cleanup" -eq 1 ]; then rm -f "$signed_apk" "$signed_apk.idsig"; fi' EXIT HUP INT TERM

cp "$unsigned_apk" "$signed_apk"

"$apksigner_bin" sign \
  --ks "$HARBOR_RELEASE_KEYSTORE" \
  --ks-key-alias "$HARBOR_RELEASE_KEY_ALIAS" \
  --ks-pass env:HARBOR_KEYSTORE_PASSWORD \
  --key-pass env:HARBOR_KEY_PASSWORD \
  "$signed_apk"

APKSIGNER_BIN="$apksigner_bin" sh scripts/verify-release-signing.sh "$signed_apk"

cleanup=0
trap - EXIT HUP INT TERM
printf 'Signed release APK: %s\n' "$signed_apk"
