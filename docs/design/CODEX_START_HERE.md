# Codex: PR #6 final cleanup

PR #6 already contains the Privacy Dashboard implementation and the follow-up interaction fixes. Do not redesign or refactor the runtime UI again.

The remaining work is **documentation cleanup, physical-device validation, and screenshot readiness**.

Read these files in this order:

1. `docs/design/PR6_FINAL_CLEANUP_PLAN.md` — authoritative remaining-work plan
2. `docs/design/PR6_FINAL_VALIDATION_CHECKLIST.md` — physical-device validation record
3. `docs/design/CODEX_PRIVACY_DASHBOARD_TASK.md` — execution checklist
4. `docs/design/UI_COMPONENT_LIBRARY.md` — reusable UI-library boundary
5. `docs/design/PR6_REVIEW_FIX_PLAN.md` — completed review-fix history
6. `docs/design/UI_PRIVACY_DASHBOARD_PLAN.md` — historical design context only

If an older plan conflicts with `PR6_FINAL_CLEANUP_PLAN.md`, **the final-cleanup plan wins**.

## Scope

Do not change runtime code unless physical-device validation exposes a concrete defect.

Do not:

- add a Settings destination or role/capability resolver;
- change `HarborRoot` ownership/profile routing;
- change DPC, DevicePolicyManager, policy, topology, Shizuku, package-management, file-sharing, APK-install, shortcut, or Advanced semantics;
- move existing operations to another controller/profile context;
- add permissions, dependencies, networking, telemetry, hidden APIs, arbitrary shell access, or destructive user deletion;
- use generated mockups as release screenshots.

## Remaining work

1. Clean stale implementation-era references from `UI_PRIVACY_DASHBOARD_PLAN.md`.
2. Confirm all design/handoff docs describe the implementation that actually exists.
3. Complete `PR6_FINAL_VALIDATION_CHECKLIST.md` on the Samsung Galaxy S24; use OnePlus 13 only as optional secondary coverage.
4. Refresh real Personal/Work screenshots after physical validation passes if this UI is shipping.
5. Run the full repository verification on the final commit.

Keep PR #6 draft until the validation checklist is complete and the final head is green.