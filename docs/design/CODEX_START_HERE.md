# Codex: PR #6 review follow-up

PR #6 already contains the Privacy Dashboard implementation. Do not redesign the architecture again.

The remaining work is a **UI-only review follow-up**.

Read these files in this order:

1. `docs/design/PR6_REVIEW_FIX_PLAN.md` — authoritative for the remaining work
2. `docs/design/CODEX_PRIVACY_DASHBOARD_TASK.md` — execution checklist
3. `docs/design/UI_COMPONENT_LIBRARY.md` — reusable UI-library boundary
4. `docs/design/UI_PRIVACY_DASHBOARD_PLAN.md` — historical design context only
5. current `PersonalProfileScreen.kt` and `WorkProfileScreen.kt`

If the older Privacy Dashboard plan conflicts with `PR6_REVIEW_FIX_PLAN.md`, **the review-fix plan wins**.

## Scope

This is presentation and interaction cleanup only.

Do not:

- add a new role-aware Settings system;
- add a Settings capability resolver;
- change `HarborRoot` ownership/profile routing;
- change DPC, DevicePolicyManager, policy, topology, Shizuku, package-management, file-sharing, APK-install, shortcut, or Advanced semantics;
- move existing operations to a different controller/profile context;
- introduce authoritative policy state in new UI preferences;
- add permissions, network dependencies, telemetry, hidden APIs, arbitrary shell access, or destructive user deletion.

The existing Work controls bottom sheet is acceptable because it is a visual relocation of the existing Work-side callbacks/controllers.

## Required review fixes

1. Restore the existing manual workspace Refresh action in the Additional workspaces UI.
2. During app selection, tapping a system app must be a no-op rather than opening its action sheet.
3. While searching, display the visible result count rather than the full catalog count.
4. For a frozen launchable app, preserve the existing shortcut wording `Add & unfreeze`.
5. Keep documentation aligned with this UI-only scope.

Use the scaffolded helpers in `ui/privacy/WorkUiReviewScaffold.kt` rather than duplicating these rules inline.

After implementation, run unit tests, lint, release build, prohibited-permission verification, and reproducibility checks. Keep the PR draft until physical-device visual validation is complete.
