# Compatibility and test matrix

## Automated targets

- Per change: JVM policy/topology/parser/privileged-output tests and compile/lint on API 36.
- Manifest permission assertions run as a host-side APK check in the normal CI job; they do not require an emulator.
- Emulator gate: API 29 and API 36 provisioning, profile-owner activation, freeze/unfreeze, reboot, and upgrade.
- Nightly target matrix: API 29, 31, 33, 35, and 36.

## Current emulator evidence

- API 36 AOSP arm64: standard consent-based provisioning passed, Harbor became profile owner, the parent opened the work instance, and a disposable package passed freeze/unfreeze verification.
- API 29 arm64 `default` and `google_apis` images report `ro.crypto.state=unsupported` and reject the production provisioning flow because Android 10 requires encryption. The DPC lifecycle and freeze/unfreeze path passed only after a temporary debug-only `PROVISIONING_SKIP_ENCRYPTION` test change. That change is not present in Harbor; standard API 29 provisioning still requires a physical encrypted device gate.
- API 36 AOSP denied UID 2000 cross-user package operations, including `install-existing --user 10`, with `Shell does not have permission to access user 10`. Shizuku ADB mode must report this as unsupported on affected builds rather than promising cloning.
- The API 29 emulator allowed a shell install targeting the managed profile. This difference confirms that shell capability must be tested per Android/OEM build, not inferred from Shizuku availability.

## Physical devices tested

- Samsung Galaxy S24 (`SM-S921B`), Android 16 (API 36): physical validation has covered Harbor's core work-profile flow. The current README screenshots were captured on this device during the alpha04 validation flow.
- OnePlus 13, Android 16 (API 36): an external F-Droid tester verified the main screen, Refresh, and Advanced screens with no crashes or ANRs. Core `Create space` provisioning was not exercised because the device already had a managed profile and Android did not allow another work profile for that parent user. Shizuku was installed but not running, so Shizuku-backed Advanced and space-management flows were not exercised.

These entries record observed test coverage, not blanket certification of every Harbor feature on those devices. The full stable-release matrix below still applies.

## Conservative compatibility behavior

- Freeze/unfreeze treats a `false` result from Android as a failed policy update and leaves the displayed state unchanged.
- The work-profile catalog includes installed user apps and launchable system apps, while filtering non-launchable system components that are not user-facing applications.
- Public profile discovery does not expose numeric user IDs. Shizuku commands accept only IDs parsed from a fresh system user listing or from the result of the allowlisted user-creation command and then revalidate them before use.
- Package cloning is enabled only when the current full user and one active managed profile can be resolved unambiguously. A sibling profile, Private Space, malformed user-list output, or an unrecognized topology disables cloning with an explanation.
- Android has no stable public deep link to one dedicated work-profile settings page across the supported OS/OEM range, so Harbor labels and opens generic System settings.
- Advanced tools can be disabled locally; this releases Harbor's Shizuku binding but does not revoke Shizuku globally.
- Harbor can clear the work-profile `DISALLOW_INSTALL_UNKNOWN_SOURCES` restriction so Android can show the normal per-source consent flow for APKs opened from Chrome, Files, F-Droid, or another source. Harbor never grants a source app permission itself; OEMs may still block sideloading.
- Harbor enables the supported personal-to-work file flow by clearing `DISALLOW_SHARE_INTO_MANAGED_PROFILE` and registering explicit `ACTION_SEND`/`ACTION_SEND_MULTIPLE` filters, including an action-only fallback for OEM senders that omit a MIME type. These are generic Android share filters, so another compatible work app may also be offered as a recipient; choosing that recipient sends it the selected file, and Harbor warns before enabling sharing. Work Harbor can open the Personal Harbor main activity through public `CrossProfileApps`; file selection then occurs locally in Personal and is forwarded through Android Share to Harbor's work-only receiver. A personal app may also select the work-profile Harbor instance directly from its Share sheet. Both paths copy the granted content URI into work-profile `Downloads/Harbor`; the personal original is never deleted. Harbor does not register work-to-personal export targets. Existing profiles receive the policy when their work-profile Harbor instance starts; OEM-specific “Move to work” and work-side personal-picker commands may still be blocked even when Android sharing is enabled.
- Work-profile app icons are loaded lazily from the local package manager with an in-memory bounded cache and a generic fallback. Icon failures do not block policy actions.
- Batch freeze/unfreeze runs sequentially and reports partial failures without rolling back successful operations. Selection is limited to the current filtered catalog and excludes Harbor/system entries from mutation.
- Clone candidates use local package labels/icons when available and fall back to the package name. Target-installed state is queried only after fresh clone-topology validation; if the query is unavailable, Harbor shows a conservative unavailable state.
- Dashboard cards for secondary Android users show only existence and `profile state unknown` until that user is opened locally. Harbor-only aliases/icons are invalidated when the observed user name changes or the user disappears.
- Pinned shortcuts are created from the work-profile Harbor instance and are bound to the target app's current signing certificate. Launcher behavior, work badges, profile pause/lock handling, and stale shortcut cleanup remain launcher/OEM dependent and require physical testing.

These behaviors are covered by host-side regression tests. Their UI and OEM command behavior still require the physical-device matrix below.

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
