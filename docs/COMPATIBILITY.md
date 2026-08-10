# Compatibility and test matrix

## Automated targets

- Per change: JVM tests and compile/lint on API 36.
- Emulator gate: API 29 and API 36 provisioning, profile-owner activation, freeze/unfreeze, reboot, and upgrade.
- Nightly target matrix: API 29, 31, 33, 35, and 36.

## Current emulator evidence

- API 36 AOSP arm64: standard consent-based provisioning passed, Harbor became profile owner, the parent opened the work instance, and a disposable package passed freeze/unfreeze verification.
- API 29 arm64 `default` and `google_apis` images report `ro.crypto.state=unsupported` and reject the production provisioning flow because Android 10 requires encryption. The DPC lifecycle and freeze/unfreeze path passed only after a temporary debug-only `PROVISIONING_SKIP_ENCRYPTION` test change. That change is not present in Harbor; standard API 29 provisioning still requires a physical encrypted device gate.
- API 36 AOSP denied UID 2000 cross-user package operations, including `install-existing --user 10`, with `Shell does not have permission to access user 10`. Shizuku ADB mode must report this as unsupported on affected builds rather than promising cloning.
- The API 29 emulator allowed a shell install targeting the managed profile. This difference confirms that shell capability must be tested per Android/OEM build, not inferred from Shizuku availability.

## Required physical testing before stable release

- Current Pixel on Android 16.
- Oldest supported Android 10 reference device.
- Current Samsung One UI.
- Current Xiaomi HyperOS.
- One current Oppo, Realme, or Vivo device.
- Current GMS-free LineageOS or equivalent.

Physical testing must cover provisioning, interrupted setup, separate work challenge, reboot/direct boot, process death, launcher badges, freeze/unfreeze, uninstall, signed DPC update, Shizuku restart, and package cloning.

Multiple full-user workspaces remain experimental until a physical Pixel and two non-Pixel OEMs successfully complete: create user, install Harbor, switch user, standard work-profile provisioning, reboot, update, and recovery.

## Known platform limits

- Stock-compatible Android provides one managed profile per parent user.
- Quiet-mode requests are not dependable for an ordinary DPC; Harbor uses the system Work toggle.
- Shizuku ADB permissions and package-manager command behavior vary by Android and OEM.
- A Shizuku server running as ADB shell may be unable to clone packages into a work profile on Android 16 even when Harbor has Shizuku permission.
- Work-profile provisioning inside a secondary full user is not guaranteed.
