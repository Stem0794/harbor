# Codex task: finish PR #6 UI review fixes

Branch: `codex/ui-privacy-dashboard`
PR: #6
Scope: **UI-only follow-up**

## Read first

1. `docs/design/PR6_REVIEW_FIX_PLAN.md`
2. `docs/design/CODEX_START_HERE.md`
3. `docs/design/UI_COMPONENT_LIBRARY.md`
4. current `PersonalProfileScreen.kt`
5. current `WorkProfileScreen.kt`
6. `app/src/main/java/com/monstera/harbor/ui/privacy/WorkUiReviewScaffold.kt`
7. `app/src/test/java/com/monstera/harbor/ui/privacy/WorkUiReviewScaffoldTest.kt`

The older `UI_PRIVACY_DASHBOARD_PLAN.md` is design history. If it conflicts with the review-fix plan, the review-fix plan is authoritative.

## Hard boundary

Do not create or refactor Harbor's policy/role architecture for this task.

Preserve:

- existing `HarborRoot` Personal-vs-Work routing;
- existing callbacks and controller ownership;
- existing DevicePolicyManager behavior;
- existing provisioning behavior;
- existing topology/user-ID trust rules;
- existing file-sharing behavior;
- existing APK-install behavior;
- existing package launch/freeze/unfreeze/details/uninstall behavior;
- existing shortcut implementation and signer validation;
- existing Advanced/Shizuku behavior;
- existing multi-user behavior.

Do not add:

- role-aware Settings architecture;
- capability resolvers;
- authoritative Personal-side Work policy state;
- new navigation/state architecture for Settings;
- new permissions/dependencies/networking/telemetry;
- hidden APIs, general shell access, or destructive user deletion.

## Implement exactly these fixes

### 1. Restore workspace Refresh

`PersonalProfileScreen` still accepts `onRefreshWorkspaces` but the redesigned UI no longer exposes it.

Add a compact Refresh action to the **Additional workspaces** section.

Requirements:

- invoke the existing `onRefreshWorkspaces` callback;
- disable while `workspaceBusy`;
- do not add new state or backend behavior;
- keep Refresh secondary rather than moving it into the main hero.

### 2. Fix system-app taps during selection

Use:

`workAppRowClickIntent(selectionMode, app.isSystem)`

Dispatch:

- `ToggleSelection` -> `onToggleSelected()`
- `OpenActions` -> `onOpenActions()`
- `NoOp` -> do nothing

Do not let a system app open its action sheet while selection mode is active.

### 3. Fix app count while searching

Use:

`workAppCountLabel(uiState.apps.size, visibleApps.size, uiState.query)`

Requirements:

- blank query -> total catalog count;
- non-blank query -> visible result count;
- do not alter search/filter behavior.

### 4. Restore frozen shortcut wording

Use:

`launcherShortcutActionLabel(app.isHidden)`

Requirements:

- hidden app -> `Add & unfreeze`;
- normal app -> `Add to launcher`;
- keep the existing shortcut callback and behavior unchanged.

### 5. Documentation consistency

Do not implement the older role-aware Settings proposal.

The current Work controls bottom sheet may stay. It is a presentation change that still calls the existing Work-side callbacks/controllers.

Do not add `HarborSettingsScreen`, a role/capability resolver, or new Settings state as part of this PR.

## Tests

Keep `WorkUiReviewScaffoldTest` green.

Add screen/Compose tests only if they are low-cost and stable. At minimum verify manually that:

- Additional workspaces exposes Refresh;
- Refresh disables while busy;
- system app tap during selection is ignored;
- user app tap during selection toggles selection;
- normal app tap opens actions;
- search count reflects visible results;
- frozen shortcut action says `Add & unfreeze`.

## Validation

Run:

```shell
./gradlew --no-daemon testDebugUnitTest lintDebug assembleRelease generateSbom
```

Also run the repository's prohibited-permission and reproducibility verification.

Inspect the final diff for:

1. removed existing operations;
2. controller/profile-context drift;
3. new policy/role state;
4. false privacy claims;
5. selection regressions;
6. shortcut semantic/copy drift;
7. design-system business logic;
8. new dependencies or permissions.

## Definition of done

- All four UI review findings are fixed.
- Workspace Refresh is restored.
- System apps are inert when tapped during selection mode.
- Search count reflects visible results.
- Frozen shortcut copy again communicates unfreeze behavior.
- No new role-aware Settings architecture exists.
- Existing policy/controller/profile behavior is unchanged.
- New scaffold tests pass.
- Full Android CI/reproducibility checks pass.
- PR remains draft until physical-device visual validation is complete.
