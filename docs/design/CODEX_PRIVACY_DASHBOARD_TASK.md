# Codex task: implement Harbor Privacy Dashboard UI

You are implementing the selected Harbor UI redesign on branch `codex/ui-privacy-dashboard`.

## Read first

1. `docs/design/CODEX_START_HERE.md`
2. `docs/design/UI_PRIVACY_DASHBOARD_PLAN.md`
3. `docs/design/PRIVACY_DASHBOARD_REVIEW.md`
4. `docs/design/UI_COMPONENT_LIBRARY.md`
5. `docs/design/harbor-ui-direction2-privacy-dashboard.svg`
6. `app/src/main/java/com/monstera/harbor/ui/HarborRoot.kt`
7. `app/src/main/java/com/monstera/harbor/ui/PersonalProfileScreen.kt`
8. `app/src/main/java/com/monstera/harbor/ui/WorkProfileScreen.kt`
9. `app/src/main/java/com/monstera/harbor/ui/WorkAppsViewModel.kt`
10. `feature/advanced/src/main/java/com/monstera/harbor/feature/advanced/AdvancedScreen.kt`
11. `docs/ARCHITECTURE.md`
12. `docs/THREAT_MODEL.md`
13. `docs/COMPATIBILITY.md`

The generated image is a style reference only. The implementation plan and review override it whenever semantics differ.

## Hard guardrails

Do not:

- add `INTERNET`;
- add telemetry/analytics/ads/accounts/Firebase/Play Services;
- add hidden APIs, direct root, arbitrary shell, or automated `dpm set-profile-owner`;
- change topology/user-ID trust rules;
- make Shizuku required for core Harbor;
- add Work→Personal sharing;
- add destructive full-user deletion;
- silently enable Advanced;
- turn failed policy operations into optimistic UI success;
- claim Work apps have no internet access;
- invent app descriptions/metadata the catalog does not provide;
- treat Personal and Work as ordinary same-process tabs;
- invoke Work-profile DPC policy operations from Personal Harbor;
- place policy/topology/privileged logic inside reusable design-system components.

## Existing behavior must remain available

Personal:

- managed-profile provisioning;
- open Work Harbor;
- personal→work file selection/share;
- optional additional-user workspace list/create/install/switch/alias/icon operations;
- platform/foreign-profile state reporting.

Work:

- search app catalog;
- launch;
- details;
- freeze/unfreeze;
- uninstall;
- signed launcher shortcut;
- select-all-visible;
- sequential batch freeze/unfreeze and partial failure reporting;
- personal-file-sharing policy enable;
- APK-install policy enable;
- open Personal Harbor.

Advanced:

- enable confirmation;
- Shizuku state/permission;
- diagnostics;
- package-clone flow;
- multi-user operations;
- backend release lifecycle.

## Reusable UI library

Use and preserve the app-internal Harbor design-system package:

`app/src/main/java/com/monstera/harbor/ui/designsystem/`

Current library guide:

`docs/design/UI_COMPONENT_LIBRARY.md`

Reuse or extend these generic primitives rather than creating parallel screen-local versions. Keep them state-light and presentation-only.

Feature-specific state and policy mapping stays outside this package, including the pure presentation mappers under `ui/privacy`.

## Implement in this order

### 1. Validate scaffold and baseline

Run:

```shell
./gradlew testDebugUnitTest lintDebug assembleDebug
```

If baseline failures exist, document them before changing UI.

Verify `PrivacyDashboardPresentationTest` and `WorkAppPresentationTest` pass.

### 2. Personal dashboard

Refactor `PersonalProfileScreen` first.

Use `resolveWorkSpaceSetupState(...)` instead of duplicating the current setup-state boolean/string logic.

Target presentation:

- top app bar: Harbor + settings/overflow action;
- hero status card;
- concise primary action;
- quick `Send files` action only when primary Work exists;
- Privacy by default card using `harborPrivacyFacts`;
- optional additional workspaces below normal content and clearly marked Advanced/experimental;
- remove the normal always-visible privilege badge;
- reuse the Harbor design-system primitives where appropriate.

Keep all existing callback signatures unless a small contract refactor materially improves testability.

### 3. Work app manager

Refactor `WorkProfileScreen`.

Target structure:

```text
Work space
[search]
N apps                         Select

[icon] App label        [status] [overflow]
[icon] App label        [status] [overflow]
...
```

Replace `ManagedAppCard` with the reusable compact Harbor app-row pattern.

Opening a row/overflow should show a Material 3 `ModalBottomSheet` or similarly clear standard component with only valid actions for that app. Use `workAppStatus(...)` and `workAppActions(...)` as the semantic baseline; adjust the mapper only if the live pre-refactor behavior proves different.

Do not use an optimistic Freeze switch.

Preserve current callback semantics and snackbars.

### 4. Selection mode

Move selection controls into the top bar/contextual region.

Requirements:

- close selection;
- selected count;
- Freeze;
- Unfreeze;
- preserve system-app exclusion;
- preserve select-all-visible;
- preserve busy/partial-failure behavior.

### 5. Role-aware Settings destination

Extend local root navigation without making profiles ordinary tabs.

Personal and Work instances may each enter a local Settings screen, but Settings capabilities must be derived from actual local profile-owner/topology state.

#### Work Harbor

Work/profile-owner Harbor may expose actionable:

```text
Work
  File sharing
  APK installation
  Android work settings

Harbor
  Privacy
  About Harbor

Advanced
  Advanced tools
  Multiple workspaces (only when contextually useful)
```

Move the existing file-sharing and APK-install policy controls from the Work app screen into Work Settings/detail content. Preserve their existing controllers, consent model, and copy constraints.

#### Personal Harbor

Personal Harbor must not call `allowPersonalFileSharing()` or `allowApkInstalls()` locally.

For those entries either:

- omit the actionable controls; or
- present an explanatory action that explicitly opens Work Harbor using the existing cross-profile path.

Do not mirror Work policy state into Personal preferences or present Personal-side toggles as authoritative.

Advanced entry must call the same opt-in confirmation path when Advanced is disabled.

### 6. Advanced visual pass

Only after Personal/Work/Settings are stable:

- align surfaces/spacing/typography with the new design;
- reuse design-system primitives where they fit;
- do not change privileged operations or backend semantics as part of visual cleanup.

### 7. Tests

At minimum add/retain coverage for:

- provisioning presentation states;
- app action availability;
- selection filtering/behavior;
- role-aware Settings capability/presentation;
- Personal Settings cannot directly invoke Work DPC policy operations;
- settings/advanced opt-in presentation;
- no invalid mutation actions for system apps;
- no unsafe create action in Android-blocked state.

### 8. Verify

Run at least:

```shell
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Also run the repository's release/reproducibility checks before completion.

Inspect the merged manifest and confirm no new network/privileged permissions were introduced.

## UX acceptance criteria

- A non-technical user can tell whether Harbor Work is ready from the first Personal screen without reading DPC terminology.
- A user opening Work sees their apps before file-sharing/APK setup utilities.
- At least twice as many app entries should fit vertically compared with the current large `ManagedAppCard` layout on the same device/font scale.
- Secondary actions no longer dominate every app row.
- Frozen/Available/Read-only states are communicated by text + visual treatment, not color alone.
- Error/destructive actions are visually distinct.
- Light and dark theme both work.
- 200% font scale does not hide the primary provisioning/manage action.
- TalkBack has labels for icon-only controls.
- No generated-mockup-only claim is introduced into production copy.
- Personal Settings does not present itself as the owner of Work DPC policy.

## Architecture acceptance criteria

- `HarborRoot` still decides Personal-vs-Work content based on real local profile-owner/topology state.
- Cross-profile launch callbacks remain explicit.
- Shizuku is still optional.
- Advanced remains opt-in.
- No user-ID guessing or privileged target inference is added to UI state.
- DPM operation results still determine visible success/failure.
- File-sharing/APK DPC changes are only performed by Work/profile-owner Harbor.
- Core policy/data/topology modules do not gain UI-specific dependencies.
- `ui/designsystem` remains presentation-only and reusable.

## Expected file shape

Exact names may change, but a good end state is approximately:

```text
app/src/main/java/com/monstera/harbor/ui/
  HarborRoot.kt
  PersonalProfileScreen.kt
  WorkProfileScreen.kt
  HarborSettingsScreen.kt
  designsystem/
    HarborUiComponents.kt
  privacy/
    PrivacyDashboardPresentation.kt
    WorkAppPresentation.kt
  theme/
    HarborTheme.kt

app/src/test/java/com/monstera/harbor/ui/
  ...presentation/action/settings mapping tests...

docs/design/
  UI_COMPONENT_LIBRARY.md
```

Avoid a single giant composable. Keep behavior callbacks at screen boundaries and visual primitives state-light.

When a genuinely reusable visual pattern is introduced, extend `ui/designsystem` rather than creating a second component family inside a feature screen.

## Final self-review before handing back

Review the diff specifically for:

1. **Semantic drift** — did any UI simplification change policy behavior?
2. **False privacy claims** — especially network isolation.
3. **Cross-profile confusion** — did navigation become fake local tabs?
4. **Settings ownership** — can Personal Harbor incorrectly mutate Work DPC policy?
5. **Privilege leakage** — did normal UI expose/trigger Advanced operations without opt-in?
6. **Optimistic policy state** — does Freeze/Share/APK show success before controller success?
7. **Action availability** — are system/hidden/non-launchable rules preserved?
8. **Accessibility** — labels, touch targets, font scale, color independence.
9. **Density** — is the Work app list actually more useful than the old card stack?
10. **Design-system consistency** — were reusable patterns added to the shared library rather than duplicated?
11. **Theme** — both system light and dark mode.
12. **Dependencies/manifest** — no unnecessary new dependency or permission.

If any of these fail, fix them before considering the refactor complete.
