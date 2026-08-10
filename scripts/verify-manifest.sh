#!/bin/sh
set -eu

apk_path=${1:?Usage: verify-manifest.sh path-to-apk}
if command -v apkanalyzer >/dev/null 2>&1 && apkanalyzer manifest permissions "$apk_path" >/tmp/harbor-manifest-permissions.$$ 2>/dev/null; then
  permissions=$(cat /tmp/harbor-manifest-permissions.$$)
  rm -f /tmp/harbor-manifest-permissions.$$
else
  rm -f /tmp/harbor-manifest-permissions.$$
  aapt_bin=${AAPT_BIN:-aapt}
  raw_permissions=$($aapt_bin dump permissions "$apk_path")
  permissions=$(printf '%s\n' "$raw_permissions" | sed -n "s/.*name='\([^']*\)'.*/\1/p")
fi

for prohibited in android.permission.INTERNET android.permission.WRITE_SECURE_SETTINGS android.permission.MANAGE_USERS android.permission.INTERACT_ACROSS_USERS android.permission.INTERACT_ACROSS_USERS_FULL; do
  if printf '%s\n' "$permissions" | grep -Fx "$prohibited" >/dev/null; then
    printf 'Prohibited permission found: %s\n' "$prohibited" >&2
    exit 1
  fi
done

printf '%s\n' "$permissions"
