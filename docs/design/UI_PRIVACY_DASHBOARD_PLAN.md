# Harbor UI refactor: Privacy Dashboard direction

> Historical design context: the current PR follows
> `docs/design/PR6_REVIEW_FIX_PLAN.md`. Preserve all existing policy ownership,
> callbacks, controllers, and profile context unchanged. Secondary controls may
> be visually reorganized, but this refactor must not introduce a new
> role/capability/settings state model.

Status: implementation plan + scaffold for Codex
Target branch: `codex/ui-privacy-dashboard`
Visual reference: `docs/design/harbor-ui-direction2-privacy-dashboard.svg`
Reusable UI library: `app/src/main/java/com/monstera/harbor/ui/designsystem/`
UI library guide: `docs/design/UI_COMPONENT_LIBRARY.md`

## Goal

Refactor Harbor's Jetpack Compose presentation toward the selected **Direction 2 — Privacy Dashboard** visual language while preserving Harbor's current provisioning, ownership, policy, topology, file-sharing, shortcut, package-management, and Shizuku semantics.

The redesign should make Harbor feel calm, trustworthy, and consumer-facing. The Personal side should answer “is my Work space ready?” immediately. The Work side should be an app manager first. Settings and Advanced tools should carry secondary and technical controls.

This is a UI refactor, not a policy rewrite.

## Non-negotiable architecture constraints

Do not change these behaviors as part of the UI work:

- Core work-profile functionality continues to use public `DevicePolicyManager` APIs.
- Core functionality remains independent from Shizuku.
- No hidden APIs, direct `su`, arbitrary shell console, or automated `dpm set-profile-owner`.
- Do not guess Android user IDs or infer privileged targets from stale UI state.
- Ambiguous privileged topology continues to fail closed.
- Do not add `INTERNET`, telemetry, analytics, ads, accounts, Firebase, Play Services, or remote configuration.
- Android remains responsible for provisioning consent and per-source APK-install consent.
- Personal → Work sharing stays explicit. Do not add Work → Personal export.
- Existing package freeze/unfreeze result handling and partial-failure semantics stay intact.
- Existing signer validation for launcher shortcuts stays intact.
- Do not expose destructive full-user deletion.
- `HarborRoot` continues to determine the local Personal-vs-Work role from actual topology/profile-owner state.
- DPC policy controls remain owned by the Work/profile-owner Harbor instance.

## Important correction to the concept image

The mockup is a **visual reference, not a source of product semantics**.

The generated concept includes wording such as “No internet” and visually implies that apps inside the Work profile cannot access the network. Harbor does **not** provide a profile-wide network firewall.

The correct claim is:

- **Harbor itself has no network permission.**

Privacy copy must describe Harbor itself unless a separate Android capability is actually enforced by current code.

Use these privacy facts:

1. `No network permission` — `Harbor itself cannot access the internet.`
2. `No analytics` — `Harbor does not collect usage or telemetry.`
3. `Local only` — `Harbor's app catalog and diagnostics stay on this device.`

The scaffold encodes these strings in `PrivacyDashboardPresentation.kt` to keep implementation code from copying the incorrect mockup claim.

## Other semantic differences from the mockup

### Cross-profile navigation

Do not implement `Personal`, `Work`, and `Settings` as ordinary same-process bottom-navigation destinations.

Harbor runs as one APK installed separately in the personal and managed profiles. Opening the other Harbor instance is a cross-profile/platform action. Keep using the existing supported navigation flow through the callbacks already supplied to `HarborRoot`.

A bottom bar is optional, but if used it must not pretend Personal and Work are ordinary in-process tabs. Recommended first implementation: use Home/Apps + Settings as local destinations and explicit `Open Work` / `Open Personal Harbor` actions for cross-profile navigation.

### Additional workspace creation

Do not show a normal `Create Work space` action beside an already-active primary Work profile unless the existing Advanced/Shizuku capability state actually permits creating a secondary full Android user. Multi-workspace functionality remains Advanced and experimental.

### Freeze control

Do not model Freeze as an optimistic switch that visually flips before `DevicePolicyManager.setApplicationHidden` succeeds. Prefer an explicit `Freeze` / `Unfreeze` action with busy state and existing result handling.

### App metadata

Do not invent descriptions such as “Private messenger” or app network status. The current catalog reliably provides app label, package identity, system/enabled/hidden/launchable state, and icon. Use only data Harbor already owns or can safely query.

## Reusable Harbor UI library

The Privacy Dashboard refactor establishes a small persistent app-internal UI library for this redesign and future Harbor UI updates.

Code lives under:

`app/src/main/java/com/monstera/harbor/ui/designsystem/`

Documentation lives at:

`docs/design/UI_COMPONENT_LIBRARY.md`

The initial library contains reusable visual primitives such as:

- layout/spacing/radius tokens;
- status tones and status pills;
- hero cards;
- generic information cards;
- action tiles;
- compact app rows;
- settings rows;
- section containers.

### Library boundary

The design system is presentation-only.

Do not place any of the following in `ui/designsystem`:

- provisioning decisions;
- profile-owner detection;
- topology/user-ID logic;
- `DevicePolicyManager` calls;
- Shizuku/privileged operations;
- package-management operations;
- file-sharing/APK policy operations;
- cross-profile navigation implementation;
- persistence or networking.

Feature semantics remain outside the library. For this refactor, provisioning and per-app action/status mapping remain under `ui/privacy`; generic visual primitives belong under `ui/designsystem`.

When a future Harbor UI update needs a card, row, status treatment, spacing token, or other reusable pattern, check and extend this library instead of creating a parallel component family.

Do not create a separate Gradle UI module as part of this refactor. A dedicated module can be considered later if the component library grows substantially or must be consumed by multiple modules.

## Current UI to migrate

### `HarborRoot.kt`

Current responsibilities to keep:

- topology refresh on resume;
- profile-owner routing;
- `DevicePolicyManager.isProvisioningAllowed(...)` check;
- Advanced opt-in confirmation;
- Shizuku lifecycle/release behavior;
- callback bridge to Android/system actions.

Refactor goal:

- replace the private two-destination `HOME / ADVANCED` presentation model with a small local navigation model that can support Settings without changing profile ownership routing;
- keep profile selection outside ordinary app navigation;
- keep Advanced opt-in confirmation at the root boundary;
- derive Settings capabilities from actual local Personal-vs-Work role.

### `PersonalProfileScreen.kt`

Current responsibilities to preserve:

- display provisioning/ownership state;
- create managed profile;
- open Work Harbor;
- send files to Work;
- display/manage optional additional Android users when privilege is available;
- workspace aliases/icons;
- surface platform-blocked states without claiming ownership of foreign profiles.

Refactor goal:

- hero card for primary Work state;
- concise quick actions;
- Harbor privacy card;
- move additional workspaces lower and visually mark them Advanced/experimental;
- remove the always-visible privilege badge from the normal dashboard;
- move implementation-level details to Settings/Advanced;
- use reusable `ui/designsystem` components where appropriate.

### `WorkProfileScreen.kt`

Current responsibilities to preserve:

- observe app catalog;
- search;
- selection + batch freeze/unfreeze;
- per-app freeze/unfreeze;
- launch;
- details;
- system-confirmed uninstall;
- signed shortcut creation;
- personal-file-sharing policy opt-in;
- APK-install policy opt-in;
- open Personal Harbor;
- snackbar result/error reporting.

Refactor goal:

- app list becomes the dominant content;
- replace large action-heavy app cards with compact rows;
- tap/overflow opens a `ModalBottomSheet` or equivalent action sheet;
- move File sharing and APK installation into Work Settings;
- contextual selection bar replaces the current selection card;
- preserve existing disabled states and operation-busy behavior;
- reuse the design-system app row/status primitives rather than creating screen-local duplicates.

### `AdvancedScreen.kt`

Keep behavior and backend ownership intact.

Refactor only enough to align visual hierarchy with the new Settings screen. Do not merge privileged behavior into normal settings rows or silently enable Advanced.

## Proposed local navigation

Recommended model:

```text
Personal Harbor instance
├── Home (Personal dashboard)
├── Settings
└── Advanced tools (opt-in destination)

Work Harbor instance
├── Apps (Work app catalog)
├── Settings
└── Advanced tools (opt-in destination)
```

Cross-profile actions remain explicit:

```text
Personal -> Open Work Harbor
Work -> Open Personal Harbor
```

Codex may implement this with a simple enum in `HarborRoot` rather than introducing Navigation Compose. Do not add a new navigation dependency unless it materially simplifies the code and passes dependency-policy review.

## Visual system

Use the Direction 2 image as the stylistic target:

- system-respecting light/dark theme;
- dark mode should feel especially polished, but light mode must remain first-class;
- deep surface layering rather than many outlined cards;
- Harbor teal as accent, not as the color of every label;
- large 24–28dp hero radius;
- approximately 18–20dp content-card radius;
- compact app rows;
- 48dp app icons;
- status pills for Ready / Available / Frozen / Disabled / Read-only;
- generous top-level spacing;
- concise copy;
- destructive actions visually separated and error-colored;
- meaningful empty states;
- no decorative network/privacy claims Harbor cannot enforce.

Reusable visual elements and tokens should be added to or reused from:

`app/src/main/java/com/monstera/harbor/ui/designsystem/HarborUiComponents.kt`

Do not move feature-specific state resolution into that library.

## Screen specifications

### 1. Personal dashboard

#### Work ready

Hero:

- title: `Work space is ready`
- body: `Your work apps and data stay separate from your personal apps.`
- primary action: `Open Work`
- secondary action: `Send files`

Below:

- Privacy by default card using the three Harbor-specific facts above.
- Additional workspaces only when existing capability/state makes the section relevant.
- Small entry to Settings.

Do not show `Create Work space` when the primary Harbor Work profile already exists.

#### Ready to create

Hero:

- title: `Set up your Work space`
- body: `Keep selected apps and their data separate from your personal apps.`
- primary action: `Create Work space`

Privacy card below.

#### Foreign profile present, provisioning still allowed

Preserve the distinction currently represented by:

`Another profile exists. Harbor will not take ownership of it.`

Show a calm warning state. Do not imply Harbor owns or can manage that profile. If Android still allows Harbor provisioning, retain the existing create action only if current behavior does so safely.

#### Blocked by existing profile

Title: `A Work profile already exists`

Body: `Android does not currently allow Harbor to create another Work profile for this user.`

Do not recommend deletion as the primary action. A Learn more/Settings action is acceptable.

#### Android blocked without a visible foreign profile

Title: `Work profile setup is unavailable`

Body: `Android does not currently allow a new Work profile on this user.`

Avoid claiming a specific cause Harbor cannot prove.

### 2. Work app manager

Header:

- `Work space`
- optional compact Harbor-local status line, for example `Managed by Harbor · Local catalog`;
- search field;
- app count;
- Select action.

App row:

- real app icon;
- label;
- status pill;
- overflow affordance;
- no permanent two-row button matrix.

Status mapping:

- hidden -> `Frozen`;
- system -> `Read-only`;
- disabled -> `Disabled`;
- otherwise -> `Available`.

Action sheet mapping:

Available user app:

- Open only if launchable;
- Freeze;
- Add to launcher only if launchable;
- App details;
- Uninstall.

Frozen user app:

- Unfreeze;
- Add to launcher where current shortcut behavior allows it;
- App details;
- Uninstall.

System app:

- Open where launchable and not hidden;
- Add to launcher where launchable;
- App details;
- no freeze/unfreeze or uninstall.

Keep current operation-busy checks and snackbar failure messages.

### 3. Selection mode

When selection mode is active:

- top bar becomes contextual;
- close action;
- `N selected`;
- Freeze;
- Unfreeze;
- optional overflow for future actions.

Do not add a large selection card into the list.

Preserve:

- system entries excluded from mutation;
- select-all-visible behavior;
- sequential batch execution;
- partial failure reporting;
- selection cleared according to current ViewModel behavior.

### 4. Secondary controls

This historical plan does not authorize a new Settings destination, role-aware
capability resolver, or policy state model. Preserve the existing operation
ownership, callbacks, controllers, and profile context exactly as implemented.

The current UI may reorganize secondary controls visually. In the implemented
Work screen, the controls bottom sheet keeps the existing Work-side callbacks
and controller calls for file sharing, APK installation, and Android Settings.
It must remain presentation-only: do not mirror policy state into new Personal
preferences, infer profile role from navigation history, or move DPC operations
to another profile instance.

### 5. Advanced tools

- preserve explicit enable confirmation;
- preserve backend state and permission request;
- preserve diagnostics;
- preserve clone topology validation;
- preserve multi-user controller behavior;
- preserve `backend.release()` lifecycle.

The new Settings entry is navigation only. It must not enable Advanced automatically.

## Scaffold files

This scaffold intentionally does **not** replace the live screens yet.

Added:

- `ui/privacy/PrivacyDashboardPresentation.kt`
  - pure setup-state resolver matching current provisioning presentation semantics;
  - accurate Harbor privacy facts.
- `ui/privacy/WorkAppPresentation.kt`
  - pure status/action mapping that freezes current per-app action semantics before the card-to-sheet migration.
- `ui/designsystem/HarborUiComponents.kt`
  - reusable Harbor visual tokens and components intended to remain for future UI updates.
- `docs/design/UI_COMPONENT_LIBRARY.md`
  - maintenance rules for the reusable UI library.
- presentation/action regression tests;
- visual reference;
- this plan;
- Codex execution checklist and review.

This lets Codex migrate one screen at a time without a large replace-everything commit while leaving behind a reusable UI foundation.

## Implementation phases

### Phase 0 — baseline

Before editing live UI:

1. Run existing tests/lint/build.
2. Record baseline failures separately from UI work.
3. Read this plan, `PRIVACY_DASHBOARD_REVIEW.md`, `CODEX_PRIVACY_DASHBOARD_TASK.md`, and `UI_COMPONENT_LIBRARY.md`.
4. Inspect the visual reference.

### Phase 1 — presentation contract + reusable design system

- keep scaffold tests green;
- use and refine `ui/designsystem` rather than creating duplicate screen-local components;
- keep the design system presentation-only;
- keep feature semantics in `ui/privacy` or screen/ViewModel boundaries;
- if changing `HarborTheme`, retain system light/dark behavior;
- do not add new UI dependencies without need;
- leave reusable components in `ui/designsystem` after the Privacy Dashboard work is complete.

### Phase 2 — Personal dashboard

- migrate `PersonalProfileScreen` to the hero/privacy-card structure;
- move current setup-state calculation to `resolveWorkSpaceSetupState`;
- retain all callbacks and workspace operations;
- hide normal privilege details from the primary view;
- keep Advanced workspaces explicitly secondary/experimental;
- compose the screen from reusable design-system primitives where practical.

### Phase 3 — Work app manager

- replace `ManagedAppCard` with the reusable compact app row;
- add app action sheet;
- preserve exact action enablement and callback semantics;
- contextualize selection mode;
- keep search/filter behavior unchanged unless adding a purely local status filter;
- promote genuinely reusable new visual patterns to `ui/designsystem` rather than duplicating them.

### Phase 4 — secondary-control presentation

- keep existing Work-side file-sharing and APK-install callbacks in their
  existing profile-owner context;
- visually group secondary controls without adding a role/capability resolver;
- keep Personal and Work routing, policy ownership, and controller semantics
  unchanged;
- retain the existing Advanced opt-in confirmation path.

### Phase 5 — Advanced visual alignment

- restyle Advanced only after core screens are stable;
- reuse design-system primitives where they fit;
- no backend/command changes unless separately justified and reviewed.

### Phase 6 — accessibility + polish

Verify:

- 48dp minimum touch targets;
- TalkBack/content descriptions for non-text actions;
- no color-only status communication;
- font scaling through at least 200% without clipped primary actions;
- light/dark contrast;
- loading/busy states;
- no tappable row conflicts with nested controls;
- bottom sheet dismissal leaves selection/action state sane.

Any accessibility/layout fix that represents a reusable pattern should improve the design-system component instead of being patched independently in every screen.

### Phase 7 — screenshots/docs

After the implementation is validated on a physical device:

- capture new Personal and Work screenshots;
- update `docs/screenshots/`;
- update Fastlane phone screenshots;
- update README screenshots if the new UI is the shipping release UI;
- update `UI_COMPONENT_LIBRARY.md` if reusable components changed materially.

Do not replace current evidence screenshots with generated mockups.

## Test plan

### Unit tests

Keep/add tests for:

- work-space setup presentation mapping;
- existing app search/filter selection logic;
- Work app status-to-action mapping;
- action availability for system/hidden/disabled/non-launchable apps;
- selection behavior remains unchanged;
- Settings capability mapping if extracted into a pure resolver.

### Compose/instrumentation tests

Add targeted tests for:

- Personal ready-to-create state exposes Create action;
- Harbor-ready state exposes Open Work and does not expose Create primary profile action;
- blocked state does not expose unsafe provisioning action;
- Work app row shows Frozen status for hidden app;
- action sheet omits invalid actions for system/non-launchable apps;
- selection mode shows count and batch actions;
- Work Settings exposes DPC policy configuration where appropriate;
- Personal Settings does not directly invoke or present authoritative local DPC policy controls;
- Personal Settings routing to Work, if implemented, uses the existing explicit cross-profile path;
- Settings Advanced entry still requires opt-in confirmation when disabled.

Do not overfit screenshot pixel tests across OEM/system font variations.

### Manual validation

At minimum on the existing Samsung Galaxy S24 test device:

- personal dashboard before provisioning;
- provisioning start/return;
- work dashboard after profile owner activation;
- app search;
- open/freeze/unfreeze/details/uninstall;
- long-press/select-all/batch operations;
- add shortcut;
- personal→work file flow;
- Work Settings file-sharing policy action;
- Work Settings APK policy action + Android consent;
- verify Personal Settings cannot perform those DPC operations locally;
- Advanced disabled/enabled/Shizuku unavailable/permission-required/ready;
- dark/light theme;
- work profile paused/locked behavior where observable.

If OnePlus 13 access is available, repeat non-destructive UI/stability checks there.

## Definition of done

The refactor is complete when:

- the UI visually follows Direction 2 without copying inaccurate product claims;
- Personal screen is status-first and understandable without DPC terminology;
- Work screen is app-list-first and substantially denser than today;
- secondary app actions are in an action sheet/menu;
- File sharing and APK controls live under Settings rather than before the app list;
- DPC policy settings are only actionable from the Work/profile-owner Harbor instance;
- Personal Settings does not attempt to own or persist authoritative Work policy state;
- Advanced remains explicit and optional;
- no policy/topology/security semantics regress;
- no new network/proprietary dependency is added;
- reusable visual primitives remain under `ui/designsystem` for future Harbor UI work;
- feature-specific policy/topology semantics have not leaked into the reusable UI library;
- all relevant tests/lint/build/reproducibility checks pass;
- generated mockup is not shipped as a factual screenshot;
- physical screenshots are refreshed only after device validation.
