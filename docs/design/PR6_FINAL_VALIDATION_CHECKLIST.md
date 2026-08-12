# PR #6 final validation checklist

Use this file to record the final physical-device validation for the Privacy Dashboard UI.

Do not mark an item as passed unless it was actually exercised on the recorded build/device.

## Validation record

- Commit SHA: `9540ebb`
- APK/build: `app/build/outputs/apk/release/app-release-unsigned.apk` — version `0.2.0-alpha05` (versionCode `7`)
- Date: `2026-08-12`
- Tester: `Codex` — automated repository validation only

### Primary device

- Device: Samsung Galaxy S24
- Android version: `NOT TESTED — no device connected`
- API level: `NOT TESTED — no device connected`

### Optional secondary device

- Device: OnePlus 13
- Android version: `NOT TESTED — no device connected`
- API level: `NOT TESTED — no device connected`

## Physical-device execution status

Physical validation is **NOT TESTED** in this run. `adb devices -l` returned no
connected devices, so no Samsung Galaxy S24 or OnePlus 13 screen, interaction,
accessibility, profile, policy, or screenshot result is marked as passed below.
The unchecked device-dependent items are intentionally retained as a checklist
for the next run with the phone connected; no runtime code was changed
speculatively and no screenshots were replaced.

## Visual validation

- [ ] Light theme: no clipping, overlap, illegible contrast, or broken surfaces.
- [ ] Dark theme: no clipping, overlap, illegible contrast, or broken surfaces.
- [ ] Large font / approximately 200%: primary actions remain visible and usable.
- [ ] Long app labels do not break the compact Work app rows.
- [ ] Bottom sheets remain scrollable and dismiss correctly.
- [ ] Status pills remain readable and are not color-only.
- [ ] Destructive actions are clearly distinguishable.

Notes:

`NOT TESTED — no physical device connected.`

## Accessibility validation

- [ ] TalkBack identifies the Work controls/overflow action.
- [ ] TalkBack identifies Exit selection.
- [ ] App selection checkboxes announce the app label.
- [ ] Text actions remain understandable without relying on icons or color.
- [ ] Touch targets are comfortably usable at normal and large font scales.

Notes:

`NOT TESTED — no physical device connected.`

## Personal dashboard

- [ ] Harbor-ready state clearly shows `Work space is ready`.
- [ ] Open Work works through the existing cross-profile path.
- [ ] Send files remains available when Work is ready.
- [ ] Ready-to-create state exposes Create Work space when Android allows provisioning.
- [ ] Blocked/unavailable state does not expose an unsafe Create action.
- [ ] Privacy card wording describes Harbor itself and does not claim Work apps are offline.
- [ ] Additional workspaces section appears only under the existing conditions.
- [ ] Additional workspaces Refresh works.
- [ ] Refresh is disabled while workspace work is busy.
- [ ] Workspace Switch / Install Harbor / Rename / Icon actions remain available where previously available.

Notes / untestable states:

`NOT TESTED — no physical device connected.`

## Work app manager

- [ ] App catalog loads.
- [ ] Search works by app label.
- [ ] Search works by package name where applicable.
- [ ] App count shows total count with a blank query.
- [ ] App count shows visible result count while searching.
- [ ] Available app status is correct.
- [ ] Frozen app status is correct.
- [ ] Read-only/system status is correct.
- [ ] Disabled status is correct where observable.
- [ ] Normal app tap opens the action sheet.
- [ ] System app tap opens valid read-only actions in normal mode.

Notes:

`NOT TESTED — no physical device connected.`

## App actions

- [ ] Open launches a launchable non-frozen app.
- [ ] Freeze succeeds and UI reflects the controller result.
- [ ] Unfreeze succeeds and UI reflects the controller result.
- [ ] App details opens Android app details.
- [ ] Uninstall invokes Android's existing confirmation flow.
- [ ] Normal launchable app shows `Add to launcher`.
- [ ] Frozen launchable app shows `Add & unfreeze`.
- [ ] Shortcut behavior remains unchanged and works where the launcher supports pinned shortcuts.
- [ ] System apps do not expose Freeze/Unfreeze or Uninstall.
- [ ] Failed operations surface existing error/snackbar behavior rather than optimistic success.

Notes:

`NOT TESTED — no physical device connected.`

## Selection mode

- [ ] Entering selection mode works.
- [ ] User app tap toggles selection.
- [ ] System app tap is a no-op during selection.
- [ ] System app checkbox is disabled.
- [ ] Select all visible excludes system apps.
- [ ] Selected count is correct.
- [ ] Batch Freeze works.
- [ ] Batch Unfreeze works.
- [ ] Existing sequential execution/partial-failure messaging remains intact.
- [ ] Exiting selection clears selection as expected.

Notes:

`NOT TESTED — no physical device connected.`

## Work controls bottom sheet

- [ ] Work controls sheet opens and dismisses correctly.
- [ ] Move files to Work opens Personal Harbor through the existing callback.
- [ ] File sharing uses the existing Work-side policy callback.
- [ ] File-sharing disclosure remains visible and accurate.
- [ ] APK installation uses the existing Work-side policy callback.
- [ ] Android still controls per-source APK consent.
- [ ] Android Work settings opens the existing system Settings destination.
- [ ] No new Personal-side policy ownership/state is introduced.

Notes:

`NOT TESTED — no physical device connected.`

## Personal → Work file flow

- [ ] File picker starts from Personal Harbor.
- [ ] Selected file is copied to the existing Work destination.
- [ ] Personal original remains intact.
- [ ] Existing Android/OEM recipient behavior remains unchanged.

Notes:

`NOT TESTED — no physical device connected.`

## Advanced / Shizuku

- [ ] Advanced remains opt-in when disabled.
- [ ] Existing confirmation wording is still shown before enabling.
- [ ] Advanced screen opens after explicit enablement.
- [ ] Shizuku unavailable/permission-required/ready presentation remains functional where testable.
- [ ] Disabling Advanced still releases the privileged backend through the existing path.
- [ ] No Advanced/Shizuku operation is required for core Work-profile usage.

Notes:

`NOT TESTED — no physical device connected.`

## Screenshot readiness

Only complete this section after the UI validation above is accepted.

- [ ] Capture final Personal screenshot on a physical device.
- [ ] Capture final Work screenshot on a physical device.
- [ ] Replace matching files under `docs/screenshots/`.
- [ ] Replace matching Fastlane phone screenshots.
- [ ] Verify README renders the updated screenshots correctly.
- [ ] Confirm generated concept images are not used as testing/release screenshots.

Screenshot notes:

`NOT TESTED — no physical device connected; existing screenshots were not replaced.`

## Final repository gate

- [x] Unit tests + lint pass on the final commit.
- [x] Release build passes.
- [x] SBOM generation passes.
- [x] Prohibited-permission verification passes.
- [x] Reproducibility verification passes (`3fafe763e92b44154823fd0f77ec3f62dca896305c04d2dd5c4354ef627c0013`).
- [x] Diff/whitespace validation passes.
- [x] No unresolved P1/P2 review finding remains in the reviewed scope.
- [x] Documentation matches the implementation actually present in PR #6.

## Final result

- [ ] PASS — ready to move PR out of draft.
- [ ] FAIL — keep PR draft and record defects below.
- [x] PARTIAL — keep PR draft; physical validation and screenshots are not tested.

Defects / blockers / untested items:

Physical blocker: no Samsung Galaxy S24 or OnePlus 13 was connected through
ADB during this run. Automated repository checks passed on commit `9540ebb`;
the final checklist edit is documentation-only.
