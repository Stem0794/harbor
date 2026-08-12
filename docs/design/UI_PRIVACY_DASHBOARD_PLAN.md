# Harbor UI refactor: Privacy Dashboard direction

Status: implementation plan + scaffold for Codex
Target branch: `codex/ui-privacy-dashboard`
Visual reference: `docs/design/harbor-ui-direction2-privacy-dashboard.svg`

## Goal

Refactor Harbor's Jetpack Compose presentation toward the selected **Direction 2 — Privacy Dashboard** visual language while preserving Harbor's current provisioning, ownership, policy, topology, file-sharing, shortcut, package-management, and Shizuku semantics.

The redesign should make Harbor feel calm, trustworthy, and consumer-facing. The Personal side should answer “is my Work space ready?” immediately. The Work side should be an app manager first. Settings and Advanced tools should carry secondary/technical controls.

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

## Important correction to the concept image

The mockup is a **visual reference, not a source of product semantics**.

The generated concept includes wording such as “No internet” and visually implies that apps inside the Work profile cannot access the network. Harbor does **not** provide a profile-wide network firewall. The correct claim is:

- **Harbor itself has no network permission.**

Privacy copy must describe Harbor itself unless a separate Android capability is actually enforced by current code.

Use these privacy facts:

1. `No network permission` — `Harbor itself cannot access the internet.`
2. `No analytics` — `Harbor does not collect usage or telemetry.`
3. `Local only` — `Harbor's app catalog and diagnostics stay on this device.`

The scaffold encodes these strings in `PrivacyDashboardPresentation.kt` to keep Codex from copying the incorrect mockup claim.

## Other semantic differences from the mockup

### Cross-profile navigation

Do not implement `Personal`, `Work`, and `Settings` as ordinary same-process bottom-navigation destinations.

Harbor runs as one APK installed separately in the personal and managed profiles. Opening the other Harbor instance is a cross-profile/platform action. Keep using the existing supported navigation flow (`CrossProfileApps` via the callbacks already supplied to `HarborRoot`).

A bottom bar is optional, but if used it must not pretend Personal and Work are ordinary in-process tabs. Recommended first implementation: use top-level Home/Apps + Settings destinations locally, and explicit `Open Work` / `Open Personal Harbor` actions for cross-profile navigation.

### Additional workspace creation

Do not show a normal `Create Work space` action beside an already-active primary Work profile unless the existing Advanced/Shizuku capability state actually permits creating a secondary full Android user. Multi-workspace functionality remains Advanced and experimental.

### Freeze control

Do not model Freeze as an optimistic switch that visually flips before `DevicePolicyManager.setApplicationHidden` succeeds. Prefer an explicit `Freeze` / `Unfreeze` action row with a busy state and existing result handling.

### App metadata

Do not invent descriptions such as “Private messenger” or app network status. The current catalog reliably provides app label, package identity, system/enabled/hidden/launchable state, and icon. Use only data that Harbor already owns or can safely query.

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
- keep Advanced opt-in confirmation at the root boundary.

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
- move implementation-level details to Settings/Advanced.

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
- move File sharing and APK installation into Settings;
- contextual selection bar replaces the current selection card;
- preserve existing disabled states and operation-busy behavior.

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
- ~18–20dp content-card radius;
- compact app rows;
- 48dp app icons;
- status pills for Ready / Available / Frozen / Disabled / Read-only;
- generous top-level spacing;
- concise copy;
- destructive actions visually separated and error-colored;
- meaningful empty states;
- no decorative network/privacy claims that Harbor cannot enforce.

The initial reusable dimensions/status primitives live in:

- `app/src/main/java/com/monstera/harbor/ui/privacy/PrivacyDashboardComponents.kt`

Codex may rename/move them once the final component boundaries are clear.

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

Do not recommend deletion as the primary action. A “Learn more”/Settings action is acceptable.

#### Android blocked without a visible foreign profile

Title: `Work profile setup is unavailable`

Body: `Android does not currently allow a new Work profile on this user.`

Avoid claiming a specific cause Harbor cannot prove.

### 2. Work app manager

Header:

- `Work space`
- optional compact Harbor-local status line, e.g. `Managed by Harbor · Local catalog`
- search field
- app count
- Select action

App row:

- real app icon;
- label;
- status pill;
- overflow affordance;
- no permanent 2-row button matrix.

Status mapping:

- hidden -> `Frozen`
- system -> `Read-only`
- disabled -> `Disabled`
- otherwise -> `Available`

Action sheet mapping:

Available user app:

- Open (only if launchable)
- Freeze
- Add to launcher (only if launchable)
- App details
- Uninstall

Frozen user app:

- Unfreeze
- Add to launcher where current shortcut behavior allows it
- App details
- Uninstall

System app:

- Open where launchable and not hidden
- Add to launcher where launchable (this is available in the current UI)
- App details
- no freeze/unfreeze or uninstall

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

### 4. Settings

Create a local Settings destination.

#### Work

- File sharing
- APK installation
- Android work settings

#### Harbor

- Privacy
- About Harbor

#### Advanced

- Advanced tools / Shizuku
- Multiple workspaces entry when relevant

Settings rows must explain state but not perform silent privilege escalation.

#### File sharing detail

Move the existing Work-screen personal→work disclosure and `allowPersonalFileSharing()` action here.

Retain the warning that generic Android cross-profile share filters may expose other compatible Work apps as recipients.

#### APK installation detail

Move `allowApkInstalls()` here.

Copy should make clear Harbor only clears its DPC restriction; Android still controls per-source consent and OEM policy may block installation.

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

- `PrivacyDashboardPresentation.kt`
  - pure setup-state resolver matching current provisioning presentation semantics;
  - accurate Harbor privacy facts;
- `PrivacyDashboardComponents.kt`
  - visual primitives for hero cards, status pills, privacy facts, action tiles, compact app rows, settings rows, and sections;
- `PrivacyDashboardPresentationTest.kt`
  - regression coverage for the setup-state resolver;
- `WorkAppPresentation.kt` + `WorkAppPresentationTest.kt`
  - pure status/action mapping that freezes the current per-app action semantics before the card-to-sheet migration;
- visual reference image;
- this plan;
- Codex execution checklist.

This lets Codex migrate one screen at a time without a large “replace everything” commit.

## Implementation phases

### Phase 0 — baseline

Before editing live UI:

1. Run existing tests/lint/build.
2. Record baseline failures separately from UI work.
3. Read this plan and `CODEX_PRIVACY_DASHBOARD_TASK.md`.
4. Inspect the visual reference.

### Phase 1 — presentation contract + components

- keep scaffold tests green;
- integrate/adjust design primitives;
- if changing the existing `HarborTheme`, retain system light/dark behavior;
- do not add new UI dependencies without need.

### Phase 2 — Personal dashboard

- migrate `PersonalProfileScreen` to the hero/privacy-card structure;
- move current setup-state calculation to `resolveWorkSpaceSetupState`;
- retain all callbacks and workspace operations;
- hide normal privilege details from the primary view;
- keep Advanced workspaces explicitly secondary/experimental.

### Phase 3 — Work app manager

- replace `ManagedAppCard` with compact row;
- add app action sheet;
- preserve exact action enablement and callback semantics;
- contextualize selection mode;
- keep search/filter behavior unchanged unless adding a purely local status filter.

### Phase 4 — Settings

- add Settings destination in `HarborRoot`;
- move file-sharing and APK-install controls from Work screen into Settings details;
- add privacy/about presentation;
- link Advanced through existing enable confirmation.

### Phase 5 — Advanced visual alignment

- restyle Advanced only after core screens are stable;
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

### Phase 7 — screenshots/docs

After the implementation is validated on a physical device:

- capture new Personal and Work screenshots;
- update `docs/screenshots/`;
- update Fastlane phone screenshots;
- update README screenshots if the new UI is the shipping release UI.

Do not replace current evidence screenshots with generated mockups.

## Test plan

### Unit tests

Keep/add tests for:

- work-space setup presentation mapping;
- existing app search/filter selection logic;
- `WorkAppPresentation` status-to-action mapping;
- action availability for system/hidden/disabled/non-launchable apps;
- selection behavior remains unchanged.

### Compose/instrumentation tests

Add targeted tests for:

- Personal ready-to-create state exposes Create action;
- Harbor-ready state exposes Open Work and does not expose Create primary profile action;
- blocked state does not expose unsafe provisioning action;
- Work app row shows Frozen status for hidden app;
- action sheet omits invalid actions for system/non-launchable apps;
- selection mode shows count and batch actions;
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
- APK policy action + Android consent;
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
- Advanced remains explicit and optional;
- no policy/topology/security semantics regress;
- no new network/proprietary dependency is added;
- all relevant tests/lint/build pass;
- generated mockup is not shipped as a factual screenshot;
- physical screenshots are refreshed only after device validation.
