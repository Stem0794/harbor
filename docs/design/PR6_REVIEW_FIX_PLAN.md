# PR #6 review follow-up plan

Status: implementation follow-up for `codex/ui-privacy-dashboard`
Scope: **UI-only fixes** from the latest PR review

This plan is authoritative for the PR #6 follow-up. It narrows the remaining work to presentation and interaction regressions found during review. It must not introduce a new settings-role model, capability resolver, policy state, controller, topology rule, or privileged behavior.

## Guardrail

Preserve all existing Harbor behavior and ownership exactly as it existed before the Privacy Dashboard refactor.

- Existing callbacks remain authoritative.
- Existing controllers/ViewModels remain authoritative.
- Existing Personal-vs-Work execution context remains unchanged.
- Existing DPC/policy ownership remains unchanged.
- The Work controls bottom sheet is only a visual relocation of existing Work-side controls.
- Do not add a new role-aware Settings system.
- Do not modify `HarborRoot` for these review fixes unless a purely visual/navigation correction is unavoidable.
- Do not change provisioning, topology, Shizuku, package-management, file-sharing, APK-install, shortcut, or Advanced semantics.

## Review findings to implement

### 1. Restore manual workspace refresh

Regression: the redesigned `PersonalProfileScreen` still receives `onRefreshWorkspaces`, but no visible control invokes it.

Implementation:

- Restore a compact `Refresh` action in the **Additional workspaces** presentation.
- Reuse the existing `onRefreshWorkspaces` callback.
- Disable the action while `workspaceBusy` is true.
- Do not add new refresh state, persistence, or backend behavior.
- Do not promote Refresh back into the primary Personal hero; keep it secondary.

Acceptance:

- Users who could manually refresh workspaces before the refactor can still do so.
- The callback path remains `workspaceViewModel::refresh` through the existing screen boundary.

### 2. Fix system-app row behavior in selection mode

Regression: tapping a system app while selection mode is active currently falls through to `onOpenActions()`.

Required behavior:

```text
selection mode + mutable user app -> toggle selection
selection mode + system app       -> no-op
normal mode                        -> open app actions
```

Scaffold:

- Use `workAppRowClickIntent(selectionMode, isSystem)` from `ui/privacy/WorkUiReviewScaffold.kt`.
- Wire `ManagedAppRow` to dispatch the returned intent.

Acceptance:

- System apps remain excluded from selection.
- Tapping a system app during selection does not open the action sheet.
- Normal-mode row taps still open the action sheet.

### 3. Show the filtered app count while searching

Regression: the heading always uses the full app count while the subtitle says `Matching apps`.

Implementation:

- Use the filtered/visible count when `uiState.query` is non-blank.
- Use the total catalog count when the query is blank.
- Keep filtering behavior itself unchanged.

Scaffold:

- Use `workAppCountLabel(totalCount, visibleCount, query)` from `ui/privacy/WorkUiReviewScaffold.kt`.

Acceptance:

- Blank query: `18 apps` means 18 total catalog apps.
- Search returning 2 results: heading says `2 apps`.
- Search/filter semantics do not change.

### 4. Preserve the hidden-app shortcut meaning

Regression/copy drift: the old hidden-app action was explicitly labeled `Add & unfreeze`; the new action sheet says `Add to launcher` even though the shortcut can restore the frozen app.

Implementation:

- Hidden launchable app: label shortcut action `Add & unfreeze`.
- Non-hidden launchable app: label `Add to launcher`.
- Keep the existing shortcut callback and signer validation unchanged.

Scaffold:

- Use `launcherShortcutActionLabel(isHidden)` from `ui/privacy/WorkUiReviewScaffold.kt`.

Acceptance:

- Copy accurately describes the existing operation.
- No shortcut behavior changes.

### 5. Remove stale role-aware implementation instructions

The current handoff documents still describe a role-aware Settings implementation that is outside the requested scope.

Implementation:

- `CODEX_START_HERE.md` must point to this review follow-up first.
- `CODEX_PRIVACY_DASHBOARD_TASK.md` must describe only the current review-fix work.
- Treat the earlier long Privacy Dashboard plan as historical design context where it conflicts with this file.
- Do not add `HarborSettingsScreen`, role/capability resolvers, authoritative Personal-side Work policy state, or new navigation/state architecture as part of this PR.

The implemented Work controls bottom sheet may remain. It is acceptable because it continues to invoke the existing Work-side callbacks/controllers and does not create a new policy model.

## Scaffold files

The review scaffold adds pure UI helpers and tests without changing runtime behavior by itself:

```text
app/src/main/java/com/monstera/harbor/ui/privacy/
  WorkUiReviewScaffold.kt

app/src/test/java/com/monstera/harbor/ui/privacy/
  WorkUiReviewScaffoldTest.kt
```

The helpers define:

- row-click intent during/ outside selection mode;
- total-vs-filtered app count label;
- hidden-vs-normal launcher shortcut label.

Wire these helpers into `WorkProfileScreen` during implementation rather than re-encoding the rules inline.

## Implementation order

1. Keep the current PR head as baseline and run unit tests/lint.
2. Restore the Additional workspaces Refresh action.
3. Wire `workAppRowClickIntent` into `ManagedAppRow`.
4. Wire `workAppCountLabel` into the Work header.
5. Wire `launcherShortcutActionLabel` into the app action sheet.
6. Update the stale Codex handoff docs to UI-only scope.
7. Run tests/lint/release build and repository reproducibility verification.
8. Perform a final diff review specifically for behavior drift.

## Test requirements

Retain all existing tests and add/keep coverage for:

- system app + selection mode -> `NoOp`;
- user app + selection mode -> `ToggleSelection`;
- normal mode -> `OpenActions`;
- blank search uses total app count;
- active search uses visible app count;
- hidden shortcut label is `Add & unfreeze`;
- normal shortcut label is `Add to launcher`.

Manual UI checks:

- Additional workspaces shows a usable Refresh action when that section is present.
- Refresh is disabled while workspace operations are busy.
- System row does nothing when tapped during selection mode.
- Search result count follows visible results.
- Frozen app action sheet says `Add & unfreeze`.
- Work controls still call the same existing callbacks/controllers.

## Definition of done

- The four review findings above are fixed.
- No new role-aware Settings/capability architecture is introduced.
- No existing Harbor operation is removed.
- No existing controller/callback/profile execution context is changed.
- No manifest/dependency/permission change is introduced.
- `ui/designsystem` remains presentation-only.
- Existing and new unit tests pass.
- Lint and release build pass.
- Prohibited-permission and reproducibility checks pass.
- PR remains draft until physical-device visual validation is complete.
