#!/bin/sh
set -eu

first_dir=$(mktemp -d)
second_dir=$(mktemp -d)
trap 'rm -rf "$first_dir" "$second_dir"' EXIT
gradle_bin=${GRADLE_BIN:-./gradlew}

"$gradle_bin" --no-daemon clean assembleRelease
cp app/build/outputs/apk/release/app-release-unsigned.apk "$first_dir/harbor.apk"
"$gradle_bin" --no-daemon clean assembleRelease
cp app/build/outputs/apk/release/app-release-unsigned.apk "$second_dir/harbor.apk"

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

printf 'Reproducible unsigned APK: %s\n' "$first_hash"
