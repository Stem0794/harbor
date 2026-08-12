# Harbor UI component library

Harbor's reusable Compose presentation primitives live in
`app/src/main/java/com/monstera/harbor/ui/designsystem/`. They are deliberately
small and feature-agnostic so visual changes do not become a second policy or
profile-management layer.

## Components

- `HarborHeroCard` — status-first setup and ready states with optional primary and secondary actions.
- `HarborInfoCard` — calm surface for explanatory content and screen-owned slots.
- `HarborPrivacyCard` — reusable, accurate privacy facts.
- `HarborStatusPill` — status text with tone and text, never color alone.
- `HarborSettingsRow` — accessible secondary action row with a 48dp-plus touch target.
- `HarborEmptyState` — intentional empty and unavailable states.
- `HarborSectionTitle` — consistent hierarchy for dense screens.
- `HarborSpacing` and `HarborShapes` — shared layout and surface tokens.

## Boundary

The design system contains no business logic, persistence, network access,
profile-owner checks, `DevicePolicyManager`, Shizuku, package operations,
cross-profile routing, or user-ID handling. Screens pass already-resolved copy,
state, and callbacks into these components. Feature-specific presentation
mappers belong under the screen's package, such as `ui/privacy`.

## Accessibility and themes

Components use Material 3 color roles so light and dark themes inherit contrast
from `HarborTheme`. Statuses include text labels and do not rely on color alone.
Icon-only actions must provide a content description. New interactive rows must
retain at least a 48dp touch target and remain usable at large font scales.
