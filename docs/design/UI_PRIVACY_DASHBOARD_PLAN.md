# Harbor UI refactor: Privacy Dashboard direction

Status: historical design context for the implemented UI refactor

The authoritative follow-up documents for PR #6 are:

1. `docs/design/PR6_FINAL_CLEANUP_PLAN.md`
2. `docs/design/PR6_FINAL_VALIDATION_CHECKLIST.md`
3. `docs/design/PR6_REVIEW_FIX_PLAN.md`

This document records the visual direction and the decisions that shaped the
implementation. It is not a current runtime task list. The implemented PR is
presentation-only: preserve existing policy ownership, callbacks, controllers,
ViewModels, profile context, and Android platform behavior.

## Goal

The Privacy Dashboard direction makes Harbor calm, status-first, and easier to
understand without changing Harbor's provisioning, profile ownership, policy,
topology, file-sharing, shortcut, package-management, or Shizuku semantics.

- Personal answers “is my Work space ready?” immediately.
- Work is an app manager first.
- Advanced/Shizuku remains optional and visibly secondary.
- Secondary Work controls are grouped visually, while still invoking the same
  Work-side callbacks and controllers.

## Current implementation map

- `app/src/main/java/com/monstera/harbor/ui/PersonalProfileScreen.kt` —
  status-first Personal dashboard, privacy card, Work entry, file action, and
  existing additional-workspace callbacks.
- `app/src/main/java/com/monstera/harbor/ui/WorkProfileScreen.kt` — searchable
  app-list-first Work manager, compact rows, contextual selection, action sheet,
  and the existing Work controls bottom sheet.
- `app/src/main/java/com/monstera/harbor/ui/designsystem/HarborDesignSystem.kt` —
  presentation-only spacing, shapes, cards, status pills, empty states, and
  settings rows.
- `app/src/main/java/com/monstera/harbor/ui/privacy/PrivacyPresentation.kt` —
  presentation copy and app-status mapping.
- `app/src/main/java/com/monstera/harbor/ui/privacy/WorkUiReviewScaffold.kt` —
  pure review-fix helpers for row intent, filtered count, and shortcut copy.
- `app/src/test/java/com/monstera/harbor/ui/privacy/` — pure presentation and
  review-fix regression tests.
- `docs/design/UI_COMPONENT_LIBRARY.md` — maintenance and accessibility rules
  for the reusable UI primitives.

## Non-negotiable behavior boundaries

The UI refactor must not:

- change public `DevicePolicyManager` behavior or provisioning consent;
- add hidden APIs, reflection, direct `su`, arbitrary shell access, or automated
  profile-owner assignment;
- infer Android user IDs or privileged targets from stale UI state;
- add network access, telemetry, analytics, ads, accounts, Firebase, Play
  Services, or remote configuration;
- change Personal → Work sharing, APK-install consent, package freeze/unfreeze,
  signer validation, batch failure handling, or Advanced/Shizuku lifecycle;
- move a DPC operation to another profile instance;
- add destructive full-user deletion.

## Semantic corrections to the concept image

The generated concept image is a visual reference only. It must not be used as
product evidence or as a source of runtime semantics.

Harbor does not provide a profile-wide network firewall. The accurate privacy
copy is:

1. **No network permission** — Harbor itself cannot access the internet.
2. **No analytics** — Harbor does not collect usage or telemetry.
3. **Local only** — Harbor's app catalog and diagnostics stay on this device.

Personal and Work are separate Android profile instances. Existing explicit
cross-profile callbacks remain the navigation boundary; they are not ordinary
same-process tabs.

## Personal dashboard direction

The Personal screen uses a status-first hero:

- ready: `Work space is ready` with `Open Work` and `Send files`;
- ready to create: `Set up your Work space` with `Create Work space` when
  Android allows provisioning;
- foreign or blocked profile: a calm warning without claiming ownership or
  offering an unsafe create action.

The privacy card follows the hero. Additional workspaces remain lower in the
hierarchy and retain their existing Refresh, Switch, Install Harbor, Rename, and
Icon callbacks. Advanced/full-user behavior is still experimental.

## Work app-manager direction

The Work screen is app-list-first:

- search and app count are prominent;
- compact rows show the real icon, label, status pill, and overflow affordance;
- status text is explicit: `Available`, `Frozen`, `Disabled`, or `Read-only`;
- action sheets preserve Open, Freeze/Unfreeze, Add to launcher/Add & unfreeze,
  App details, and Uninstall enablement;
- system apps remain read-only and are inert when tapped during selection mode;
- selection mode preserves select-all-visible, batch operations, busy states,
  and partial-failure reporting.

The Work controls bottom sheet is only a visual relocation of existing Work-side
controls for file sharing, APK installation, Personal Harbor navigation, and
Android Work settings. Preserve all existing policy ownership, callbacks,
controllers, and profile context unchanged. Secondary controls may be visually
reorganized, but this refactor must not introduce a new role/capability/settings
state model.

## Design-system boundary

`ui/designsystem` is presentation-only. It may contain visual tokens and
reusable Compose components, but not provisioning decisions, profile ownership,
topology/user-ID logic, `DevicePolicyManager`, Shizuku, package operations,
file-sharing/APK policy calls, navigation ownership, persistence, or networking.

Use Material 3 color roles so light and dark themes retain contrast. Statuses
must include text and never rely on color alone. Interactive rows and icon-only
actions need accessible labels and at least 48dp touch targets. Components must
remain usable at large font scales.

## Historical decisions and exclusions

Earlier drafts explored local Settings navigation and role-aware capability
presentation. Those are historical context only, not implementation
requirements for PR #6. The current UI-only work keeps existing root routing,
operation ownership, callbacks, controllers, and profile context unchanged.

Likewise, generated mockups are not release screenshots. Real Personal and Work
screenshots may replace the existing evidence only after physical-device
validation is recorded in `PR6_FINAL_VALIDATION_CHECKLIST.md`.

## Validation gate

Automated validation for the final head includes:

```text
./gradlew --no-daemon testDebugUnitTest lintDebug assembleRelease generateSbom
```

Also run the repository manifest/prohibited-permission check,
`scripts/verify-reproducible.sh`, and `git diff --check`. Physical validation
on the Samsung Galaxy S24 covers themes, large font scale, TalkBack, Personal
and Work flows, action sheets, selection mode, Work controls, APK/file flows,
and screenshot readiness. Any unavailable device state must be recorded as
`NOT TESTED` with its reason rather than guessed or “fixed” speculatively.
