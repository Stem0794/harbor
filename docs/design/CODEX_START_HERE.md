# Codex: PR #6 Direction 2 visual-fidelity pass

PR #6 already preserves the required Harbor behavior, but the current Compose implementation does **not** visually match the supplied Direction 2 raster mockup closely enough.

The next pass is a **UI-only pixel-fidelity implementation**.

## Read first

1. `docs/design/PR6_DIRECTION2_PIXEL_FIDELITY_PLAN.md` — **authoritative implementation plan**
2. the user-supplied `harbor-direction2-reference.png` raster mockup — **authoritative visual reference**
3. `docs/design/CODEX_PRIVACY_DASHBOARD_TASK.md` — execution checklist
4. `docs/design/UI_COMPONENT_LIBRARY.md` — reusable presentation boundary
5. current `PersonalProfileScreen.kt` and `WorkProfileScreen.kt`
6. `docs/design/PR6_REVIEW_FIX_PLAN.md` — completed behavior-regression history
7. `docs/design/UI_PRIVACY_DASHBOARD_PLAN.md` — historical design context only

If an older cleanup/design document conflicts with `PR6_DIRECTION2_PIXEL_FIDELITY_PLAN.md`, **the pixel-fidelity plan wins**.

Do not use `docs/design/harbor-ui-direction2-privacy-dashboard.svg` as the fidelity target. It is a simplified historical scaffold and is materially less detailed than the supplied raster.

## Scope

Rebuild the visible Personal and Work presentation so real Samsung Galaxy S24 dark-mode screenshots closely reproduce the raster: lighthouse branding and hero artwork, exact dark/teal palette, full-width cyan CTA, quick-action tiles, structured privacy panel, mockup-style navigation chrome, Work header/search/list, status pills, and icon-driven app action bottom sheet.

Preserve all existing Harbor policy/profile/controller behavior. Do not add a role-aware Settings architecture, policy state model, new permissions, networking, telemetry, hidden APIs, general shell access, or destructive user deletion.

The few semantic corrections documented in the pixel-fidelity plan remain mandatory; otherwise visual simplification is not acceptable.

Keep PR #6 draft until real-device side-by-side comparison with the raster is accepted and the full repository checks pass.
