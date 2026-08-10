#!/bin/sh
set -eu

apk_path=${1:?Usage: verify-manifest.sh path-to-apk}
permissions=$(apkanalyzer manifest permissions "$apk_path")

for prohibited in android.permission.INTERNET android.permission.WRITE_SECURE_SETTINGS android.permission.MANAGE_USERS android.permission.INTERACT_ACROSS_USERS; do
  if printf '%s\n' "$permissions" | grep -Fx "$prohibited" >/dev/null; then
    printf 'Prohibited permission found: %s\n' "$prohibited" >&2
    exit 1
  fi
done

printf '%s\n' "$permissions"
