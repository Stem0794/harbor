# Privacy Dashboard scaffold review

This review is part of the Codex handoff. Read it together with `UI_PRIVACY_DASHBOARD_PLAN.md` and `CODEX_PRIVACY_DASHBOARD_TASK.md`.

## Review result

The scaffold is intentionally **presentation-only and unwired**. It adds no live navigation, screen replacement, policy operation, manifest permission, dependency, privileged command, or Shizuku behavior.

The implementation should continue from the scaffold rather than treating the generated concept image as authoritative behavior.

## Corrections already made from the visual concept

### 1. No false network-isolation claim

The concept image visually suggested that Work apps have no Internet access. Harbor does not implement a profile-wide firewall.

Production copy must say only what Harbor actually guarantees, for example:

- `No network permission` — `Harbor itself cannot access the internet.`
- `No analytics` — `Harbor does not collect usage or telemetry.`
- `Local only` — `Harbor's app catalog and diagnostics stay on this device.`

### 2. Personal and Work are not normal tabs

Harbor is installed separately in the personal and managed profiles. Keep explicit cross-profile launch callbacks. Do not replace this with fake in-process Personal/Work tab navigation.

### 3. Do not expose primary Work creation when Work is already ready

The mockup's second `Create Work space` action should not be copied into the normal ready state. Additional full-user workspaces remain Advanced/Shizuku-dependent and experimental.

### 4. Freeze is not an optimistic switch

The current ViewModel updates hidden state only after the controller reports success. The redesigned action sheet should use explicit `Freeze` / `Unfreeze` actions with busy/result handling rather than a switch that flips immediately.

### 5. Do not invent app metadata

The concept included descriptions such as `Private messenger`. Harbor's current catalog model contains package name, label, system/enabled/hidden/launchable state, and icon. Do not invent descriptions, network state, privacy ratings, or storage/version fields unless a separate reviewed data source is intentionally added.

### 6. Work policy settings are work-instance operations

This is a critical ownership rule for the new Settings screen.

`allowPersonalFileSharing()` and `allowApkInstalls()` are DPC/profile-owner policy operations and must remain actionable only from the Work Harbor instance where Harbor is the profile owner.

If the user opens Settings in the Personal Harbor instance:

- do not call those policy operations locally;
- either omit those active controls, or show an explanatory row/action that opens Work Harbor to configure them;
- do not duplicate policy state optimistically in Personal preferences.

`HarborRoot` must continue to determine the local Personal-vs-Work role from actual profile-owner/topology state.

### 7. Per-app action availability is frozen in a pure mapper

`WorkAppPresentation.kt` mirrors the existing `ManagedAppCard` action availability so the card-to-bottom-sheet migration does not silently change behavior.

Notable current behavior preserved by the mapper:

- hidden app: no Open, but Unfreeze and Add to launcher can remain available when launchable;
- system app: no freeze/unfreeze or uninstall;
- launchable system app: Open and Add to launcher remain available;
- non-launchable user app: Freeze/Unfreeze, Details, and Uninstall remain possible as applicable.

If Codex believes the mapper is wrong, compare it against the pre-refactor `WorkProfileScreen.kt` before changing it.

## Scaffold code review

### Good boundaries

- New Compose components are `internal` and state-light.
- No existing screen imports them yet, so runtime behavior is unchanged.
- Provisioning presentation is extracted into a pure resolver with regression tests.
- Work app status/action presentation is extracted into pure functions with regression tests.
- No new Gradle dependency is required for the scaffold.
- No icon library was added; Codex can use existing app icons and standard Material components during integration.

### Integration cautions

- The scaffold components are starting primitives, not a requirement to keep every function unchanged.
- Keep app rows genuinely compact when integrating; the final Work list should be substantially denser than the current large cards.
- Avoid nested click targets that make row taps and overflow/actions ambiguous for accessibility.
- Do not move controller calls into generic visual components. Keep policy/system callbacks at screen boundaries.
- Keep `backend.release()` and Advanced opt-in lifecycle behavior in the existing root/Advanced ownership boundaries.

## Required Codex validation

Before declaring the UI refactor complete:

1. Run `./gradlew testDebugUnitTest lintDebug assembleDebug`.
2. Run the repository's release/reproducibility checks when practical.
3. Inspect the merged manifest and verify no prohibited/network permission was added.
4. Verify Personal ready/create/blocked/foreign-profile states.
5. Verify Work app actions for available, frozen, system, disabled, and non-launchable apps.
6. Verify batch freeze/unfreeze partial-failure behavior.
7. Verify file sharing and APK policy actions from the Work instance only.
8. Verify Advanced disabled, permission-required, unavailable, and ready states.
9. Verify light/dark theme, TalkBack labels, 48dp touch targets, and 200% font scale.
10. Capture real device screenshots only after the implementation is validated; do not use the generated visual reference as release evidence.
