# Harbor UI component library

Harbor keeps a small app-internal Compose component library for reusable visual primitives introduced by the Privacy Dashboard refactor and future UI work.

Code location:

`app/src/main/java/com/monstera/harbor/ui/designsystem/`

Current component file:

`HarborUiComponents.kt`

## Purpose

The library exists so future Harbor UI changes reuse the same visual language instead of recreating cards, spacing, status treatments, and rows in individual screens.

It is intentionally small. Harbor does not need a separate published design-system module at this stage.

## Current reusable elements

- `HarborUiLayout`
  - screen padding;
  - section/content spacing;
  - major/card/row corner radii;
  - standard app icon size.
- `HarborStatusTone`
  - positive;
  - frozen;
  - neutral;
  - warning;
  - danger.
- `HarborStatusPill`
- `HarborHeroCard`
- `HarborInfoCard`
- `HarborInfoItem`
- `HarborActionTile`
- `HarborCompactAppRow`
- `HarborSettingsRow`
- `HarborSection`

The Privacy Dashboard may add more elements when a pattern is genuinely reused across screens.

## Library boundary

These components must stay presentation-only.

Do not put any of the following into `ui/designsystem`:

- `DevicePolicyManager` calls;
- provisioning decisions;
- profile-owner detection;
- topology/user-ID logic;
- Shizuku or privileged operations;
- package-management operations;
- file-sharing or APK policy changes;
- cross-profile navigation implementation;
- feature-specific permission/state assumptions;
- persistence/networking.

Those concerns remain at screen, ViewModel, controller, repository, topology, or root-navigation boundaries.

A reusable component may receive already-resolved labels, status tones, callbacks, enabled states, and content slots. It must not decide whether Harbor is allowed to perform a privileged/policy operation.

## Feature-specific presentation

Feature semantics stay outside the library.

For the Privacy Dashboard refactor:

- provisioning presentation mapping remains under `ui/privacy`;
- Work app action/status mapping remains under `ui/privacy`;
- reusable cards/rows/tokens live under `ui/designsystem`.

For example, the design system can render a `HarborStatusPill("Frozen", HarborStatusTone.FROZEN)`, but `ui/privacy` or the Work screen determines whether a given `ManagedApp` is actually Frozen.

## When to add a component

Add or promote a component to the library when at least one of these is true:

1. the same visual interaction appears on two or more Harbor screens;
2. consistency matters enough that duplicating it would create design drift;
3. it represents a stable Harbor visual primitive such as status, section layout, app row, or settings row.

Do not move one-off screen layouts into the library just to reduce file size.

## Component requirements

Reusable components should:

- use Material 3 and `HarborTheme` colors/typography;
- support light and dark themes;
- preserve at least 48dp interactive touch targets where applicable;
- work with TalkBack and content descriptions supplied by callers;
- avoid color-only status communication;
- tolerate large font scales;
- accept state/callbacks instead of owning business behavior;
- avoid new dependencies unless there is a clear repository-wide benefit.

## Future evolution

Keep the library under the `app` module while Harbor remains a single primary Compose application surface.

If the component set becomes large or multiple modules need to render the same Harbor design system, a future refactor may extract it into a dedicated UI module. That should be a deliberate architecture change, not part of an unrelated screen redesign.

Whenever a future UI update introduces a reusable pattern, check this library first and extend it instead of creating a parallel component family.
