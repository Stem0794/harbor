# PR #6 final cleanup plan

Status: final cleanup handoff for `codex/ui-privacy-dashboard`
Scope: documentation cleanup, physical-device validation, and screenshot readiness only

This plan is authoritative for the remaining work on PR #6 after the UI review fixes were implemented.

## Boundary

Do not redesign or refactor Harbor again as part of this cleanup.

Do not change runtime code unless physical-device validation exposes a concrete defect that cannot be resolved through documentation or screenshots alone.

Preserve:

- current Personal and Work UI behavior;
- existing callbacks, controllers, ViewModels, and profile execution context;
- existing DPC/DevicePolicyManager behavior;
- existing topology and profile-owner logic;
- existing file-sharing and APK-install behavior;
- existing shortcut behavior and signer validation;
- existing Advanced/Shizuku behavior;
- existing permissions, dependencies, and manifest.

Do not add:

- a Settings destination or role/capability resolver;
- new policy state or preferences;
- new navigation architecture;
- new dependencies or permissions;
- new product claims;
- generated mockups as release screenshots.

## Remaining work

### 1. Clean historical design documentation

`docs/design/UI_PRIVACY_DASHBOARD_PLAN.md` is historical context, but it still contains stale implementation-era references.

Clean it so future contributors cannot mistake obsolete ideas for current requirements.

Remove or rewrite references to:

- a new local Settings navigation model;
- Personal/Work Settings destinations that were not implemented;
- role-aware Settings/capability resolvers;
- Work Settings / Personal Settings acceptance tests;
- obsolete scaffold filenames such as `PrivacyDashboardPresentation.kt`, `WorkAppPresentation.kt`, and `HarborUiComponents.kt`;
- statements that the scaffold has not yet replaced the live screens;
- any Definition-of-Done item that requires an unimplemented Settings architecture.

Replace stale filenames with the current implementation where useful:

- `app/src/main/java/com/monstera/harbor/ui/privacy/PrivacyPresentation.kt`
- `app/src/main/java/com/monstera/harbor/ui/privacy/WorkUiReviewScaffold.kt`
- `app/src/main/java/com/monstera/harbor/ui/designsystem/HarborDesignSystem.kt`
- `app/src/main/java/com/monstera/harbor/ui/PersonalProfileScreen.kt`
- `app/src/main/java/com/monstera/harbor/ui/WorkProfileScreen.kt`

The historical plan should describe the implemented Work controls bottom sheet as a presentation-only relocation of existing Work-side controls.

### 2. Keep the handoff documents consistent

After cleaning the historical plan, verify these documents all agree:

- `docs/design/CODEX_START_HERE.md`
- `docs/design/CODEX_PRIVACY_DASHBOARD_TASK.md`
- `docs/design/PR6_REVIEW_FIX_PLAN.md`
- `docs/design/UI_COMPONENT_LIBRARY.md`
- `docs/design/UI_PRIVACY_DASHBOARD_PLAN.md`

The consistent message must be:

- the UI refactor is implemented;
- prior runtime review findings are fixed;
- no new role-aware Settings architecture is required;
- current remaining work is validation/documentation/screenshots only.

### 3. Perform physical-device validation

Use `docs/design/PR6_FINAL_VALIDATION_CHECKLIST.md` as the validation record.

Primary device:

- Samsung Galaxy S24

Optional secondary validation if available:

- OnePlus 13

Validate at minimum:

- light theme;
- dark theme;
- approximately 200% font scale;
- TalkBack labels for icon-only controls;
- Personal setup/ready/blocked presentation states that can safely be exercised;
- Work app search and filtered count;
- normal app action sheet;
- system app behavior during selection;
- Freeze / Unfreeze;
- App details;
- Uninstall flow;
- Add to launcher / Add & unfreeze wording and behavior;
- select-all-visible and batch actions;
- Work controls bottom sheet;
- Personal → Work file flow;
- APK-install policy flow and Android consent;
- Advanced disabled/enabled entry flow;
- no obvious clipping, overlap, inaccessible actions, or broken scrolling.

If a validation item cannot be safely tested on the available device, mark it `NOT TESTED` with the reason rather than changing code speculatively.

### 4. Refresh screenshots only after validation passes

After the physical-device UI is accepted:

- replace the Personal screenshot in `docs/screenshots/`;
- replace the Work screenshot in `docs/screenshots/`;
- update matching Fastlane phone screenshots;
- retain the existing README screenshot references if filenames stay unchanged;
- update README copy only if the final UI makes existing screenshot captions inaccurate.

Use real device screenshots only.

Do not commit generated visual mockups as release evidence.

### 5. Final repository verification

After documentation/screenshot changes, require the normal repository checks to pass again:

- unit tests + lint;
- release build;
- SBOM generation;
- prohibited-permission verification;
- reproducibility verification;
- `git diff --check` or equivalent whitespace/diff validation.

Documentation-only cleanup should not require runtime-code changes to make CI pass.

## Scaffold

The final-cleanup scaffold consists of:

```text
docs/design/
  PR6_FINAL_CLEANUP_PLAN.md
  PR6_FINAL_VALIDATION_CHECKLIST.md
```

The checklist is intentionally a fill-in validation record. Record the tested commit SHA and actual device/OS before marking items complete.

## Definition of done

PR #6 is ready to move out of draft when:

- stale Settings-era instructions are removed from the historical plan;
- the design docs consistently describe the UI-only implementation that actually exists;
- physical-device validation is recorded;
- no unresolved P1/P2 review finding remains;
- any P3 documentation issue is resolved;
- real screenshots are refreshed if this UI is intended to ship immediately;
- the latest PR head passes the full Android workflow and reproducibility checks.

If physical validation exposes an actual defect, document the defect separately and make the smallest targeted fix necessary. Do not reopen the architecture/design scope.