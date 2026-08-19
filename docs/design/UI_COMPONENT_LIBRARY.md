# Harbor UI component library

Harbor's reusable Compose presentation primitives live in
`app/src/main/java/com/monstera/harbor/ui/designsystem/`. They are deliberately
small and feature-agnostic so visual changes do not become a second policy or
profile-management layer.

## Components

- `HarborHeader` and `HarborBrandMark` — branded Personal/Work chrome and lighthouse/briefcase identity.
- `HarborHeroBackground` — lighthouse, beam, waves, and accent artwork.
- `HarborQuickActionTile` — the compact Send files / setup or Advanced tiles.
- `HarborPrivacyPanel` — structured privacy facts with leading icons, dividers, and confirmation marks.
- `HarborBottomBar` — presentation-only Personal/Work/Settings navigation shell.
- `HarborSearchField` — filled rounded Work catalog search surface.
- `HarborIcon` and `HarborIconButton` — project-owned line icons with accessible descriptions.
- `HarborStatusPill` — status text with tone and text, never color alone.
- `HarborColors`, `HarborSpacing`, and `HarborShapes` — Harbor's palette, rhythm, and surface tokens.

Work app rows and action sheets remain screen-owned because they bind real
`ManagedApp` state and callbacks. They use the same Harbor primitives and must
not move policy or package operations into this library.

## Boundary

The design system contains no business logic, persistence, network access,
profile-owner checks, `DevicePolicyManager`, Shizuku, package operations,
cross-profile routing, or user-ID handling. Screens pass already-resolved copy,
state, and callbacks into these components. Feature-specific presentation
mappers belong under the screen's package, such as `ui/privacy`.

## Accessibility and themes

Components use Material 3 color roles so light and dark themes inherit contrast
from `HarborTheme`. Statuses include text labels and do not rely on color alone.
Icon-only actions must provide a content description. Icons next to an equivalent
visible text label are decorative and must not repeat that label to TalkBack. New
interactive rows must retain at least a 48dp touch target and remain usable at
large font scales.
