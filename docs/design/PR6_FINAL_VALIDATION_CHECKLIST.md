# PR #6 final validation checklist

Use this file to record the final physical-device validation for the Privacy Dashboard UI.

Do not mark an item as passed unless it was actually exercised on the recorded build/device.

## Validation record

- Latest runtime-changing code head: `36fc9f0`
- Latest physically validated Personal runtime: `851e0cc`
- Latest physically validated Work runtime: `851e0cc`
- APK/build: `app/build/outputs/apk/release/app-release-unsigned.apk` — version `0.2.0-alpha05` (versionCode `7`)
- Date: `2026-08-13`
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

Physical validation is **PARTIAL**. The final signed build from `851e0cc` was
installed for both profiles with `adb install -r <apk>` and no `--user`
argument. Personal user `0` and Work profile user `12` remained installed, and
the real Open Work callback opened the final Work instance. The final Personal
hero, Work header/catalog, and search surface were exercised on the Samsung S24.
Selection, large-font Work actions, and bottom-sheet
interaction checks remain pending.

## Visual validation

- [x] Light theme: no clipping, overlap, illegible contrast, or broken surfaces.
- [x] Dark theme: no clipping, overlap, illegible contrast, or broken surfaces.
- [x] Personal large font / approximately 200%: primary actions remain visible and usable after scrolling.
- [x] Long app labels do not break the compact Work app rows.
- [ ] Bottom sheets remain scrollable and dismiss correctly (interaction not exercised in this pass).
- [x] Status pills remain readable and are not color-only.
- [x] Destructive actions are clearly distinguishable.

Notes:

Samsung Galaxy S24 / Android 16 screenshots were captured at 1080×2340. Harbor
keeps the Direction 2 surfaces dark when Android switches to light mode; the
screen remained readable. At 200% font scale, the final build's primary CTA and
quick-action tiles remained reachable by scrolling; the bottom navigation stayed
usable and labels remained readable. Work catalog scrolling was exercised on the
final Work runtime; bottom-sheet interaction remains pending.

Targeted Direction 2 polish validation on the same signed alpha05 build:

- [x] Lighthouse beam apex is at the right-side lantern and fans leftward.
- [x] Personal `Work space is ready` hero restores the blue/teal lighthouse artwork surface.
- [x] Normal Work action sheet shows the corrected launcher-shortcut icon.
- [x] Active Work selection mode shows the symmetric Close icon.
- [x] Freeze/unfreeze was exercised on Agenda and the app was restored to Available.
- [ ] `Add & unfreeze` was not exposed by the device after freezing Agenda because
  Android no longer reported that package as launchable; the same `Shortcut`
  icon path is used by the existing conditional action in source.

The current PR head adds the pure Personal hero-action resolver, selection-mode
Close icon, state-tone hero treatment, conditional divider rendering, and
content-safe large-font layout. The final Personal APK was installed and
checked; blocked and foreign-profile warning states were not reproduced because
the real Work profile was not removed.

## Accessibility validation

- [ ] TalkBack identifies the Work controls/overflow action.
- [ ] TalkBack identifies Exit selection.
- [ ] App selection checkboxes announce the app label.
- [ ] Text actions remain understandable without relying on icons or color.
- [ ] Touch targets are comfortably usable at normal and large font scales.

Notes:

Automated Compose semantics tests passed on the Samsung S24 for quick actions,
privacy facts, and bottom navigation without duplicate icon descriptions.
An actual TalkBack walkthrough remains untested; large-font Personal validation
is recorded below.

Large-font validation was performed on the final Personal runtime and passed
for the Personal hero, CTA, quick actions, and bottom navigation. TalkBack
validation remains untested. Final-build Work large-font selection and sheet
checks remain pending even though the final Work APK is now installed.

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

Persistent-header and Work accessibility checks:

- [x] Personal branded header is visible on the full-width blue/teal surface and remains visible while dashboard content scrolls.
- [x] Work branded header is visible on the full-width blue/teal surface and remains visible while app catalog scrolls.
- [ ] Entering selection replaces the Work header with the selection header.
- [ ] Exiting selection restores the Work branded header.
- [ ] Selection header remains usable at approximately 200% font.
- [ ] All selection actions remain visible/reachable.
- [ ] App action sheet remains scrollable at approximately 200% font.
- [ ] Work controls sheet remains scrollable at approximately 200% font.
- [ ] Work navigation sheet remains scrollable at approximately 200% font.

Notes / untestable states:

On the Samsung S24, Personal showed the active Harbor-managed Work state and
the Manage action launched the final `com.monstera.harbor/.MainActivity` as
user 12. The Personal file action was visible. Provisioning, blocked states,
and additional-workspace cases remain untested.

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

The Work screen rendered `14 apps`, real package labels/icons, and the final
Search apps field without the removed inert Filter affordance on Samsung S24 /
Android 16. The Work instance was reached through the final Personal build's
Open Work callback after the successful all-user replacement.

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
the conditional `Add & unfreeze` path remain untested. Samsung rejected a
direct per-user replacement for user `12`, but the later all-user
`adb install -r` replacement succeeded and the final Work instance was opened.

## Selection mode

- [x] Entering selection mode works.
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

Selection mode was entered on the active Work instance and the Close icon was
visible. Batch operations and checkbox semantics remain untested.

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
- [x] Capture a physical Work screenshot from a previously validated runtime.
- [x] Capture a final-current-build Work screenshot.
- [x] Replace matching files under `docs/screenshots/`.
- [x] Replace matching Fastlane phone screenshots.
- [ ] Verify README renders the updated screenshots correctly.
- [ ] Confirm generated concept images are not used as testing/release screenshots.

Screenshot notes:

The Personal hero and Work screenshot were captured from the final signed
runtime on the Samsung S24. The Work screenshot shows the current blue/teal
header and app catalog after the successful all-user USB replacement. README
rendering was not exercised here.

## Application identity

- [x] Harbor launcher icon uses the Direction 2 lighthouse mark (confirmed in Samsung app-info on the final Personal APK).
- [x] No legacy anchor reference remains in the application manifest.
- [x] Adaptive icon mask/cropping checked on Samsung One UI.
- [ ] Round icon checked on Samsung One UI.
- [ ] Work-profile Harbor shows the new lighthouse identity where observable.
- [ ] Android 13+ themed icon checked where available.
- [x] Lighthouse foreground fits the intended adaptive-icon safe area.
- [x] No important geometry is cropped by One UI.

## Final repository gate

- [x] Unit tests + lint pass on the final commit.
- [x] Release build passes.
- [x] SBOM generation passes.
- [x] Prohibited-permission verification passes.
- [x] Reproducibility verification passes (`e5fbc957eed20f17e9e051ff7b9e8709484ef7aad66323bc3894f450dea58767`).
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
