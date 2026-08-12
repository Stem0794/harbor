# PR #6 final validation checklist

Use this file to record the final physical-device validation for the Privacy Dashboard UI.

Do not mark an item as passed unless it was actually exercised on the recorded build/device.

## Validation record

- Commit SHA: `<fill in>`
- APK/build: `<fill in>`
- Date: `<fill in>`
- Tester: `<fill in>`

### Primary device

- Device: Samsung Galaxy S24
- Android version: `<fill in>`
- API level: `<fill in>`

### Optional secondary device

- Device: OnePlus 13
- Android version: `<fill in or NOT TESTED>`
- API level: `<fill in or NOT TESTED>`

## Visual validation

- [ ] Light theme: no clipping, overlap, illegible contrast, or broken surfaces.
- [ ] Dark theme: no clipping, overlap, illegible contrast, or broken surfaces.
- [ ] Large font / approximately 200%: primary actions remain visible and usable.
- [ ] Long app labels do not break the compact Work app rows.
- [ ] Bottom sheets remain scrollable and dismiss correctly.
- [ ] Status pills remain readable and are not color-only.
- [ ] Destructive actions are clearly distinguishable.

Notes:

`<fill in>`

## Accessibility validation

- [ ] TalkBack identifies the Work controls/overflow action.
- [ ] TalkBack identifies Exit selection.
- [ ] App selection checkboxes announce the app label.
- [ ] Text actions remain understandable without relying on icons or color.
- [ ] Touch targets are comfortably usable at normal and large font scales.

Notes:

`<fill in>`

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

`<fill in>`

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

`<fill in>`

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

`<fill in>`

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

`<fill in>`

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

`<fill in>`

## Personal → Work file flow

- [ ] File picker starts from Personal Harbor.
- [ ] Selected file is copied to the existing Work destination.
- [ ] Personal original remains intact.
- [ ] Existing Android/OEM recipient behavior remains unchanged.

Notes:

`<fill in>`

## Advanced / Shizuku

- [ ] Advanced remains opt-in when disabled.
- [ ] Existing confirmation wording is still shown before enabling.
- [ ] Advanced screen opens after explicit enablement.
- [ ] Shizuku unavailable/permission-required/ready presentation remains functional where testable.
- [ ] Disabling Advanced still releases the privileged backend through the existing path.
- [ ] No Advanced/Shizuku operation is required for core Work-profile usage.

Notes:

`<fill in>`

## Screenshot readiness

Only complete this section after the UI validation above is accepted.

- [ ] Capture final Personal screenshot on a physical device.
- [ ] Capture final Work screenshot on a physical device.
- [ ] Replace matching files under `docs/screenshots/`.
- [ ] Replace matching Fastlane phone screenshots.
- [ ] Verify README renders the updated screenshots correctly.
- [ ] Confirm generated concept images are not used as testing/release screenshots.

Screenshot notes:

`<fill in>`

## Final repository gate

- [ ] Unit tests + lint pass on the final commit.
- [ ] Release build passes.
- [ ] SBOM generation passes.
- [ ] Prohibited-permission verification passes.
- [ ] Reproducibility verification passes.
- [ ] Diff/whitespace validation passes.
- [ ] No unresolved P1/P2 review finding remains.
- [ ] Documentation matches the implementation actually present in PR #6.

## Final result

- [ ] PASS — ready to move PR out of draft.
- [ ] FAIL — keep PR draft and record defects below.
- [ ] PARTIAL — keep PR draft; list untested blockers below.

Defects / blockers / untested items:

`<fill in>`
