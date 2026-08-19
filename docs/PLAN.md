# Plan

## Beta

- Add generic cross-profile app access management using Android's public `DevicePolicyManager` APIs.
  - Expose a per-app "Cross-profile access" control for apps installed in both the personal and managed profiles.
  - On Android 11+ (API 30+), manage the profile-owner allowlist with `getCrossProfilePackages()` and `setCrossProfilePackages()` without replacing unrelated existing entries.
  - Keep access disabled by default and require explicit user action before an app is allowlisted.
  - Let Android handle the final user-consent flow for connecting personal and work instances.
  - Keep this policy separate from Harbor's existing personal-to-work file-sharing intent filters.
  - Initial compatibility target: microG Services (`com.google.android.gms`), resolving issue #8 on Android 16 / LineageOS 23.x where "Connected work and personal apps" cannot be enabled until the profile owner permits the package.
  - Use only standard profile-owner APIs; no Shizuku, hidden APIs, or privileged cross-user permissions.
