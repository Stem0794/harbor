#!/bin/sh
set -eu

first_dir=$(mktemp -d)
second_dir=$(mktemp -d)
trap 'rm -rf "$first_dir" "$second_dir"' EXIT
gradle_bin=${GRADLE_BIN:-./gradlew}

if ! command -v unzip >/dev/null 2>&1; then
  printf 'unzip is required to inspect release APK metadata\n' >&2
  exit 1
fi

verify_vcs_metadata() {
  apk=$1
  if ! metadata=$(unzip -p "$apk" META-INF/version-control-info.textproto 2>/dev/null); then
    printf 'Release APK is missing valid VCS metadata\n' >&2
    exit 1
  fi

  if printf '%s\n' "$metadata" | grep -Fq 'NO_VALID_GIT_FOUND'; then
    printf 'Release APK contains invalid Git metadata: NO_VALID_GIT_FOUND\n' >&2
    exit 1
  fi
}

"$gradle_bin" --no-daemon --no-parallel --no-configuration-cache clean
"$gradle_bin" --no-daemon --no-parallel --no-configuration-cache assembleRelease
cp app/build/outputs/apk/release/app-release-unsigned.apk "$first_dir/harbor.apk"
verify_vcs_metadata "$first_dir/harbor.apk"

"$gradle_bin" --no-daemon --no-parallel --no-configuration-cache clean
"$gradle_bin" --no-daemon --no-parallel --no-configuration-cache assembleRelease
cp app/build/outputs/apk/release/app-release-unsigned.apk "$second_dir/harbor.apk"
verify_vcs_metadata "$second_dir/harbor.apk"

hash_file() {
  if command -v sha256sum >/dev/null 2>&1; then
    sha256sum "$1" | cut -d ' ' -f 1
  else
    shasum -a 256 "$1" | cut -d ' ' -f 1
  fi
}

first_hash=$(hash_file "$first_dir/harbor.apk")
second_hash=$(hash_file "$second_dir/harbor.apk")

if [ "$first_hash" != "$second_hash" ]; then
  printf 'Release APKs differ: %s != %s\n' "$first_hash" "$second_hash" >&2
  exit 1
fi

printf 'Deterministic unsigned APK: %s\n' "$first_hash"
