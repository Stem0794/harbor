# Codex task: implement Harbor Privacy Dashboard UI

You are implementing the selected Harbor UI redesign on branch `codex/ui-privacy-dashboard`.

## Read first

1. `docs/design/UI_PRIVACY_DASHBOARD_PLAN.md`
2. `docs/design/harbor-ui-direction2-privacy-dashboard.svg`
3. `app/src/main/java/com/monstera/harbor/ui/HarborRoot.kt`
4. `app/src/main/java/com/monstera/harbor/ui/PersonalProfileScreen.kt`
5. `app/src/main/java/com/monstera/harbor/ui/WorkProfileScreen.kt`
6. `app/src/main/java/com/monstera/harbor/ui/WorkAppsViewModel.kt`
7. `feature/advanced/src/main/java/com/monstera/harbor/feature/advanced/AdvancedScreen.kt`
8. `docs/ARCHITECTURE.md`
9. `docs/THREAT_MODEL.md`
10. `docs/COMPATIBILITY.md`

The generated image is a style reference only. The implementation plan overrides it whenever semantics differ.

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
- invent app descriptions/metadata that the catalog does not provide;
- treat Personal and Work as ordinary same-process tabs.

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
- remove the normal always-visible privilege badge.

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

Replace `ManagedAppCard` with a compact row.

Opening a row/overflow should show a Material 3 `ModalBottomSheet` (or a similarly clear standard component) with only valid actions for that app. Use `workAppStatus(...)` and `workAppActions(...)` as the semantic baseline; adjust the mapper only if the live pre-refactor behavior proves different.

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

### 5. Settings destination

Extend local root navigation without making profiles ordinary tabs.

Personal and Work instances may each enter a local Settings screen.

Settings sections:

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

Move the Work-screen sharing and APK cards into Settings/detail content while preserving their existing controllers and copy constraints.

Advanced entry must call the same opt-in confirmation path when Advanced is disabled.

### 6. Advanced visual pass

Only after Personal/Work/Settings are stable:

- align surfaces/spacing/typography with the new design;
- do not change privileged operations or backend semantics as part of visual cleanup.

### 7. Tests

At minimum add/retain coverage for:

- provisioning presentation states;
- app action availability if extracted to a pure mapper;
- selection filtering/behavior;
- settings/advanced opt-in presentation;
- no invalid mutation actions for system apps;
- no unsafe create action in Android-blocked state.

### 8. Verify

Run at least:

```shell
./gradlew testDebugUnitTest lintDebug assembleDebug
```

If repository release checks are practical in the environment, also run the documented release verification commands.

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

## Architecture acceptance criteria

- `HarborRoot` still decides Personal-vs-Work content based on real local profile-owner/topology state.
- Cross-profile launch callbacks remain explicit.
- Shizuku is still optional.
- Advanced remains opt-in.
- No user-ID guessing or privileged target inference is added to UI state.
- DPM operation results still determine visible success/failure.
- Core policy/data/topology modules do not gain UI-specific dependencies.

## Expected file shape

Exact names may change, but a good end state is approximately:

```text
app/src/main/java/com/monstera/harbor/ui/
  HarborRoot.kt
  PersonalProfileScreen.kt
  WorkProfileScreen.kt
  HarborSettingsScreen.kt
  privacy/
    PrivacyDashboardPresentation.kt
    PrivacyDashboardComponents.kt
    WorkAppPresentation.kt
  theme/
    HarborTheme.kt

app/src/test/java/com/monstera/harbor/ui/
  ...presentation/action mapping tests...
```

Avoid a single giant composable. Keep behavior callbacks at screen boundaries and visual primitives state-light.

## Final self-review before handing back

Review the diff specifically for:

1. **Semantic drift** — did any UI simplification change policy behavior?
2. **False privacy claims** — especially network isolation.
3. **Cross-profile confusion** — did navigation become fake local tabs?
4. **Privilege leakage** — did normal UI expose/trigger Advanced operations without opt-in?
5. **Optimistic policy state** — does Freeze/Share/APK show success before controller success?
6. **Action availability** — are system/hidden/non-launchable rules preserved?
7. **Accessibility** — labels, touch targets, font scale, color independence.
8. **Density** — is the Work app list actually more useful than the old card stack?
9. **Theme** — both system light and dark mode.
10. **Dependencies/manifest** — no unnecessary new dependency or permission.

If any of these fail, fix them before considering the refactor complete.
