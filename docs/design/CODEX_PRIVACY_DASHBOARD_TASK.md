# Codex task: finish PR #6 cleanup and validation

Branch: `codex/ui-privacy-dashboard`
PR: #6
Scope: **documentation cleanup + physical-device validation + screenshots**

## Read first

1. `docs/design/PR6_FINAL_CLEANUP_PLAN.md`
2. `docs/design/PR6_FINAL_VALIDATION_CHECKLIST.md`
3. `docs/design/CODEX_START_HERE.md`
4. `docs/design/UI_COMPONENT_LIBRARY.md`
5. `docs/design/UI_PRIVACY_DASHBOARD_PLAN.md`

The runtime UI review fixes are already implemented. Do not redo them.

## Hard boundary

Do not change runtime code unless device validation exposes a concrete defect.

Preserve all existing:

- Personal/Work routing;
- callbacks and controller ownership;
- DevicePolicyManager behavior;
- provisioning behavior;
- topology/user-ID trust rules;
- file-sharing behavior;
- APK-install behavior;
- app launch/freeze/unfreeze/details/uninstall behavior;
- shortcut behavior and signer validation;
- Advanced/Shizuku behavior;
- multi-user behavior;
- manifest permissions and dependencies.

Do not add a Settings architecture, capability resolver, policy state model, new navigation framework, permission, dependency, network capability, telemetry, hidden API, arbitrary shell access, or destructive user deletion.

## 1. Clean the historical plan

Edit `docs/design/UI_PRIVACY_DASHBOARD_PLAN.md` so it remains useful historical context without containing stale instructions that look current.

Remove/rewrite:

- proposed local Settings navigation;
- Personal/Work Settings destinations;
- role-aware Settings/capability-resolver requirements;
- Work Settings / Personal Settings test requirements;
- obsolete scaffold filenames;
- statements that live screens have not yet been migrated;
- Definition-of-Done requirements for unimplemented Settings architecture.

Use current implementation names where relevant:

- `PrivacyPresentation.kt`
- `WorkUiReviewScaffold.kt`
- `HarborDesignSystem.kt`
- `PersonalProfileScreen.kt`
- `WorkProfileScreen.kt`

Describe the Work controls bottom sheet as presentation-only relocation of existing Work-side callbacks/controllers.

## 2. Cross-check documentation

Verify these files agree with the final UI-only scope:

- `CODEX_START_HERE.md`
- `PR6_FINAL_CLEANUP_PLAN.md`
- `PR6_REVIEW_FIX_PLAN.md`
- `UI_COMPONENT_LIBRARY.md`
- `UI_PRIVACY_DASHBOARD_PLAN.md`

Do not expand the scope while reconciling wording.

## 3. Physical-device validation

Complete `PR6_FINAL_VALIDATION_CHECKLIST.md` on the actual build under test.

Required primary device:

- Samsung Galaxy S24

Optional secondary device:

- OnePlus 13

Record:

- commit SHA;
- APK/build;
- Android version/API level;
- tester/date;
- PASS / FAIL / NOT TESTED for each relevant item;
- notes for any item that cannot be exercised safely.

Do not make speculative code changes for an untestable state.

## 4. Screenshots

Only after the physical validation is accepted:

- capture real Personal and Work screens;
- replace matching files under `docs/screenshots/`;
- replace matching Fastlane phone screenshots;
- verify README rendering;
- do not use the generated concept mockup as release evidence.

If the existing screenshot filenames remain unchanged, prefer replacing the image files rather than rewriting README references unnecessarily.

## 5. Final verification

On the final commit, require:

```shell
./gradlew --no-daemon testDebugUnitTest lintDebug assembleRelease generateSbom
```

Also require:

- prohibited-permission verification;
- reproducibility verification;
- diff/whitespace validation.

## Definition of done

- Historical plan no longer contains misleading Settings-era instructions.
- Documentation matches the implementation present in PR #6.
- Physical-device validation is recorded.
- No unresolved P1/P2 review finding remains.
- Screenshots are refreshed if this UI is intended to ship immediately.
- Full CI and reproducibility checks pass on the latest head.
- PR remains draft until the physical validation checklist is complete.

If device testing exposes a real defect, document it first and make the smallest targeted fix possible. Do not reopen the architecture/design scope.