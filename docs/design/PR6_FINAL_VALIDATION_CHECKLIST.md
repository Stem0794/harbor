# PR #6 final validation checklist

Use this file to record the final physical-device validation for the Privacy Dashboard UI.

Do not mark an item as passed unless it was actually exercised on the recorded build/device.

## Validation record

- Runtime build validated for the recorded physical smoke test: `dba967f`
- Current PR code head: `74d601c` (hero action/tone polish; not yet installed on the S24)
- APK/build: `app/build/outputs/apk/release/app-release-unsigned.apk` — version `0.2.0-alpha05` (versionCode `7`)
- Date: `2026-08-12`
- Tester: `Codex` — automated checks plus Samsung Galaxy S24 smoke test via ADB

### Primary device

- Device: Samsung Galaxy S24
- Android version: `16`
- API level: `36`
- Work profile exercised as Android user `12`.

### Optional secondary device

- Device: OnePlus 13
- Android version: `NOT TESTED — no OnePlus 13 connected`
- API level: `NOT TESTED — no OnePlus 13 connected`

## Physical-device execution status

Physical validation is **PARTIAL**. The connected Samsung Galaxy S24 was used
for a signed alpha05 install, Personal Harbor launch, Open Work navigation,
Work app-catalog rendering, and an app action-sheet capture. The remaining
unchecked items require deliberate manual interaction or a second OEM and are
intentionally retained below.

## Visual validation

- [ ] Light theme: no clipping, overlap, illegible contrast, or broken surfaces.
- [x] Dark theme: no clipping, overlap, illegible contrast, or broken surfaces.
- [ ] Large font / approximately 200%: primary actions remain visible and usable.
- [x] Long app labels do not break the compact Work app rows.
- [ ] Bottom sheets remain scrollable and dismiss correctly.
- [ ] Status pills remain readable and are not color-only.
- [x] Destructive actions are clearly distinguishable.

Notes:

Samsung Galaxy S24 / Android 16 dark-theme screenshots were clear at 1080×2340.
The Work app action sheet was also captured; light theme, large-font, and
bottom-sheet dismissal cases remain untested.

Targeted Direction 2 polish validation on the same signed alpha05 build:

- [x] Lighthouse beam apex is at the right-side lantern and fans leftward.
- [x] Normal Work action sheet shows the corrected launcher-shortcut icon.
- [x] Freeze/unfreeze was exercised on Agenda and the app was restored to Available.
- [ ] `Add & unfreeze` was not exposed by the device after freezing Agenda because
  Android no longer reported that package as launchable; the same `Shortcut`
  icon path is used by the existing conditional action in source.

The current PR head adds the pure Personal hero-action resolver, selection-mode
Close icon, state-tone hero treatment, and conditional divider rendering. Those
changes are covered by unit tests, but the S24 was not connected for a
final-current-head install during this pass.

## Accessibility validation

- [ ] TalkBack identifies the Work controls/overflow action.
- [ ] TalkBack identifies Exit selection.
- [ ] App selection checkboxes announce the app label.
- [ ] Text actions remain understandable without relying on icons or color.
- [ ] Touch targets are comfortably usable at normal and large font scales.

Notes:

TalkBack and large-font accessibility checks remain untested on the device.

## Personal dashboard

- [x] Harbor-ready state clearly shows `Work space is ready`.
- [x] Open Work works through the existing cross-profile path.
- [x] Send files remains available when Work is ready.
- [ ] Ready-to-create state exposes Create Work space when Android allows provisioning.
- [ ] Blocked/unavailable state does not expose an unsafe Create action.
- [x] Privacy card wording describes Harbor itself and does not claim Work apps are offline.
- [ ] Additional workspaces section appears only under the existing conditions.
- [ ] Additional workspaces Refresh works.
- [ ] Refresh is disabled while workspace work is busy.
- [ ] Workspace Switch / Install Harbor / Rename / Icon actions remain available where previously available.

Notes / untestable states:

On the Samsung S24, Personal showed the active Harbor-managed Work state and
the Manage action launched `com.monstera.harbor/.MainActivity` as user 12.
The Personal file action was visible. Provisioning, blocked states, and
additional-workspace cases remain untested.

## Work app manager

- [x] App catalog loads.
- [ ] Search works by app label.
- [ ] Search works by package name where applicable.
- [x] App count shows total count with a blank query.
- [ ] App count shows visible result count while searching.
- [ ] Available app status is correct.
- [ ] Frozen app status is correct.
- [ ] Read-only/system status is correct.
- [ ] Disabled status is correct where observable.
- [x] Normal app tap opens the action sheet.
- [ ] System app tap opens valid read-only actions in normal mode.

Notes:

The Work screen rendered `14 apps`, real package labels/icons, and the Search
apps field on Samsung S24 / Android 16. Search filtering and per-app state
operations remain untested.

## App actions

- [ ] Open launches a launchable non-frozen app.
- [x] Freeze succeeds and UI reflects the controller result.
- [x] Unfreeze succeeds and UI reflects the controller result.
- [ ] App details opens Android app details.
- [ ] Uninstall invokes Android's existing confirmation flow.
- [ ] Normal launchable app shows `Add to launcher`.
- [ ] Frozen launchable app shows `Add & unfreeze`.
- [ ] Shortcut behavior remains unchanged and works where the launcher supports pinned shortcuts.
- [ ] System apps do not expose Freeze/Unfreeze or Uninstall.
- [ ] Failed operations surface existing error/snackbar behavior rather than optimistic success.

Notes:

On the recorded S24 runtime, Agenda was frozen and then unfrozen; the catalog
returned it to Available. Open, App details, Uninstall, launcher pinning, and
the conditional `Add & unfreeze` path remain untested. The current-head
selection Close icon is unit/build validated but not physically exercised.

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

Selection mode and batch operations remain untested on the physical device.

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

The Work controls content was visible, including the file-sharing disclosure
and APK consent guidance. Opening each callback/system destination was not
performed.

## Personal → Work file flow

- [ ] File picker starts from Personal Harbor.
- [ ] Selected file is copied to the existing Work destination.
- [ ] Personal original remains intact.
- [ ] Existing Android/OEM recipient behavior remains unchanged.

Notes:

No file was copied during this smoke test; the end-to-end file flow remains
untested.

## Advanced / Shizuku

- [x] Advanced remains opt-in when disabled.
- [x] Existing confirmation wording is still shown before enabling.
- [ ] Advanced screen opens after explicit enablement.
- [ ] Shizuku unavailable/permission-required/ready presentation remains functional where testable.
- [ ] Disabling Advanced still releases the privileged backend through the existing path.
- [ ] No Advanced/Shizuku operation is required for core Work-profile usage.

Notes:

Opening Advanced on the Work instance displayed the explicit Shizuku/ADB/root
confirmation dialog. Enablement, binder lifecycle, and privileged operations
were not exercised.

## Screenshot readiness

Only complete this section after the UI validation above is accepted.

- [x] Capture final Personal screenshot on a physical device.
- [x] Capture final Work screenshot on a physical device.
- [x] Replace matching files under `docs/screenshots/`.
- [x] Replace matching Fastlane phone screenshots.
- [ ] Verify README renders the updated screenshots correctly.
- [ ] Confirm generated concept images are not used as testing/release screenshots.

Screenshot notes:

Screenshots were re-captured from the signed alpha05 APK for runtime
`dba967f` on Samsung S24 / Android 16, then copied to the matching docs and
Fastlane paths. They do not claim that the later current-head integration
changes were installed. README rendering was not exercised here.

## Final repository gate

- [x] Unit tests + lint pass on the final commit.
- [x] Release build passes.
- [x] SBOM generation passes.
- [x] Prohibited-permission verification passes.
- [x] Reproducibility verification passes (`1517e27db110c0b3ae6fb59e9f98734a0ee8f83e3714fe49fc1f9426b55bd094`).
- [x] Diff/whitespace validation passes.
- [x] No unresolved P1/P2 review finding remains in the reviewed scope.
- [x] Documentation matches the implementation actually present in PR #6.

## Final result

- [ ] PASS — ready to move PR out of draft.
- [ ] FAIL — keep PR draft and record defects below.
- [x] PARTIAL — keep PR draft; only the documented physical smoke subset passed.

Defects / blockers / untested items:

The OnePlus 13 and the unmarked manual/accessibility/action cases remain
untested. The UI-fidelity implementation was exercised on the connected
Samsung S24; keep the PR draft until the remaining review cases are accepted.
