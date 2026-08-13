# PR #6 — Direction 2 pixel-fidelity implementation plan

Status: authoritative implementation plan for the next PR #6 pass
Branch: `codex/ui-privacy-dashboard`
Scope: **UI-only visual fidelity pass**
Reference: the user-supplied raster mockup `harbor-direction2-reference.png` (1122×1402)

## 0. Authority and required input

The supplied raster mockup is the **visual source of truth** for this pass.

Priority order:

1. `harbor-direction2-reference.png` — authoritative appearance, composition, branding, spacing, colors, imagery, and visual hierarchy.
2. This file — authoritative mapping from that appearance to Harbor's real callbacks/data/behavior.
3. Current Harbor runtime behavior — authoritative for policy, profile ownership, package actions, file sharing, APK installation, Shizuku, and topology.
4. Existing `docs/design/harbor-ui-direction2-privacy-dashboard.svg` — historical/simplified reference only. **Do not use it as the fidelity target.**
5. Current PR screenshots — baseline evidence of what must improve, not a design target.

Codex must have the raster reference available while implementing. If the raster is not available in the coding session, stop the visual-fidelity work rather than approximating from the simplified SVG.

## 1. Objective

Rebuild the current Personal and Work Compose screens so that, at normal phone size in dark mode, they reproduce the supplied Direction 2 mockup as closely as practical:

- same visual identity;
- same lighthouse branding;
- same dark layered surfaces;
- same teal/cyan accent treatment;
- same hero composition;
- same CTA proportions;
- same quick-action tile treatment;
- same privacy-row treatment;
- same Work header/search/list density;
- same app-row hierarchy;
- same status-pill treatment;
- same bottom-sheet composition;
- same icon-driven actions;
- same destructive-action styling.

This pass is **not** another information-architecture redesign. It is a visual implementation of the chosen design.

## 2. Fidelity contract

The current generic Material treatment is not sufficient. The result should be recognizable as the supplied mockup at a glance before reading any text.

### Required visual fidelity

For the primary dark-mode Personal and Work screenshots:

- major element position: within approximately ±4dp of the reference composition after normalizing for device aspect ratio;
- major corner radii: within approximately ±2dp;
- principal colors: visually indistinguishable in side-by-side comparison; use the sampled tokens below as the starting values;
- button/row/search heights: within approximately ±4dp;
- text hierarchy and weight: visibly equivalent;
- icon size/alignment: visibly equivalent;
- spacing rhythm: equivalent;
- bottom-sheet height, top radius, drag handle, dividers, and action spacing: equivalent;
- no generic component should visibly break the design language.

Do not declare the pass complete based only on unit tests or code structure. The merge gate includes side-by-side screenshots on the Samsung Galaxy S24.

## 3. Behavior boundary — visual match without policy drift

Preserve all existing Harbor behavior:

- `HarborRoot` profile routing;
- provisioning and Android consent;
- profile-owner/DPC ownership;
- Work app catalog/search;
- freeze/unfreeze controller result handling;
- batch sequential operations and partial-failure behavior;
- launch/details/uninstall callbacks;
- shortcut implementation and signer validation;
- Personal → Work file flow;
- Work-side file-sharing callback;
- Work-side APK-install callback;
- Advanced/Shizuku behavior;
- topology/user-ID trust rules;
- manifest permissions and dependencies unless a visual resource requires no runtime permission.

Do not add a role-aware Settings architecture, capability resolver, new policy state model, networking, analytics, telemetry, hidden APIs, arbitrary shell access, or destructive user deletion.

### The only intentional semantic substitutions from the mockup

The visual layout should remain the same, but these mockup semantics must not be copied literally:

1. **`No internet`**
   Do not claim Work apps are offline. Render the same privacy row visually, but use:
   - title: `No network permission`
   - detail: `Harbor itself cannot access the internet.`

2. **Ready-state `Create Work space` tile**
   Do not offer primary Work-profile creation when Harbor's primary Work profile already exists. Preserve the two-tile visual geometry, but map the second tile to an existing safe action such as `Advanced`/local secondary UI. In a not-yet-provisioned state, the same tile geometry may show `Create Work space` and invoke the existing provisioning callback.

3. **Personal / Work navigation**
   They are separate Android profile instances. A mockup-style `Work` navigation affordance may exist, but it must invoke the existing explicit cross-profile launch callback rather than changing a same-process tab.

4. **Freeze switch**
   The Work action sheet may visually use the switch shown in the mockup, but its checked state must come from authoritative `app.isHidden`. Trigger the existing ViewModel/controller on user action and do **not** flip local UI state optimistically before success.

No other visual simplification should be justified by these semantic corrections.

## 4. Current implementation gap to remove

The current PR uses standard `TopAppBar`, `OutlinedTextField`, generic `Card` surfaces, generic Material buttons, literal text glyphs such as `⋮`, and identical chevron-style `HarborSettingsRow` actions. That preserves behavior but does not reproduce the reference.

This pass should replace those visible generic treatments on the Personal and Work primary surfaces with purpose-built reusable Direction 2 components.

Do not keep a component merely because it already exists if it prevents fidelity. Refactor `ui/designsystem` as needed while keeping it presentation-only.

## 5. Reference palette

The following colors were sampled from the supplied raster and are the starting visual tokens. Small adjustments are allowed only when a real-device screenshot comparison demonstrates that display/color-management differences require them.

| Token | Initial value | Use |
|---|---:|---|
| `HarborBgDeep` | `#060C10` | Work/main deepest background |
| `HarborBgPersonal` | `#081319` | Personal screen background |
| `HarborSurfaceLow` | `#0E161B` | Search field / subtle inset surface |
| `HarborSurface` | `#121F27` | Privacy rows / navigation surfaces |
| `HarborSurfaceRaised` | `#151E25` | Work app rows |
| `HarborSheet` | `#19232A` | Modal bottom sheet |
| `HarborStroke` | `#2A373E` | separators and subtle outlines |
| `HarborHeroStart` | `#003A40` | hero teal gradient |
| `HarborHeroEnd` | `#00282F` | hero dark gradient |
| `HarborAccent` | `#12C8C7` | primary cyan CTA / active accents |
| `HarborAccentDark` | `#052B30` | text/icon on bright cyan |
| `HarborTextPrimary` | `#F0F2F2` | primary text |
| `HarborTextSecondary` | `#A5B6B8` | secondary text |
| `HarborTextMuted` | `#708589` | tertiary/supporting text |
| `HarborPositiveBg` | `#17302F` | Available pill background |
| `HarborPositive` | `#58D6B7` | Available pill text/accent |
| `HarborFrozenBg` | `#1F3448` | Frozen pill background |
| `HarborFrozen` | `#7CC8E8` | Frozen pill text/icon |
| `HarborDanger` | `#D85858` | Uninstall/destructive action |

Do not simply map these to Material 3 defaults and accept the resulting color. Define explicit Direction 2 visual tokens inside Harbor's presentation layer.

Light mode remains supported, but **dark mode is the pixel-fidelity reference**. Build a coherent light counterpart after dark fidelity is achieved.

## 6. Typography

Do not add a proprietary font dependency. Use Android/system sans (Roboto-compatible) and tune size, weight, line height, and letter spacing to the mockup.

Recommended starting tokens:

| Role | Size | Weight | Notes |
|---|---:|---:|---|
| App/header title | 22sp | 500–600 | `Harbor`, `Work space` |
| Hero title | 32sp | 650–700 | two-line ready state |
| Hero body | 15sp | 400 | 21–22sp line height |
| Primary CTA | 17sp | 600 | dark text on cyan |
| Card heading | 16sp | 600 | Privacy heading |
| Privacy row title | 15sp | 500–600 | |
| Privacy detail | 13sp | 400 | muted |
| Work app label | 17sp | 500 | |
| Work app secondary | 13sp | 400 | package name/data-backed text only |
| Status pill | 12sp | 500–600 | |
| Sheet app title | 18sp | 600 | |
| Sheet action | 16sp | 400–500 | |
| Bottom-nav label | 12sp | 500 | active cyan |

Tune from screenshots; do not accept default `MaterialTheme.typography` sizes when they visibly differ.

## 7. Shape and dimension tokens

Starting values:

- screen horizontal padding: `20.dp`;
- top header content height: approximately `64.dp` excluding system status bar;
- header logo tile: `36.dp`;
- hero radius: `24–28.dp`;
- hero internal padding: `24.dp`;
- primary CTA height: `56.dp`;
- primary CTA radius: `18.dp`;
- quick-action tile radius: `18.dp`;
- quick-action tile height: approximately `88–94.dp`;
- main content-card radius: `20.dp`;
- privacy-row internal vertical padding: `14–16.dp`;
- bottom navigation radius/top treatment: approximately `20.dp`;
- Work search height: `52–56.dp`;
- Work search radius: `18.dp`;
- app-row radius: `18.dp`;
- app-row minimum height: approximately `78–84.dp`;
- app icon: `48.dp`;
- status-pill height: approximately `30.dp`;
- action-sheet top radius: `28.dp`;
- action-sheet drag handle: approximately `44×4.dp`;
- action row: `56–62.dp`;
- divider: `1.dp` using `HarborStroke`.

Use responsive width and insets rather than hard-coded screenshot pixels.

## 8. Branding and visual assets

The raster reference shows a lighthouse Harbor identity. The current `app/src/main/res/drawable/ic_harbor.xml` anchor artwork does **not** visually match the reference and must not be used as the primary in-screen Direction 2 mark.

Create dedicated UI assets for Direction 2. Do not change policy behavior to do this.

### Required brand assets

1. `harbor_direction2_mark`
   - rounded dark-teal square;
   - cyan lighthouse/harbor line art;
   - used beside `Harbor` in Personal header;
   - also usable as the small Work identity mark where appropriate.

2. `harbor_direction2_work_mark`
   - same visual family;
   - small briefcase/work treatment matching the right-phone header;
   - this is an in-app visual mark, not a replacement for Android's OS-managed work badge semantics.

3. `harbor_lighthouse_hero`
   - lighthouse silhouette on the right side;
   - cyan/teal light beam extending left;
   - subtle waves at bottom;
   - tiny bird/star accents;
   - designed to sit behind text without reducing contrast;
   - no baked-in text or button;
   - use a vector/Compose Canvas asset if fidelity is practical, otherwise a lossless/high-quality WebP/PNG asset authored from the reference.

4. UI icon set matching the reference:
   - shield/check;
   - send/paper-plane;
   - plus/workspace;
   - lock/privacy;
   - network/no-network;
   - analytics;
   - local-device;
   - search;
   - filter/sliders;
   - overflow;
   - menu;
   - Open;
   - Freeze/snowflake;
   - launcher shortcut;
   - App details/info;
   - Uninstall/trash;
   - Personal/home;
   - Work/briefcase;
   - Settings/gear.

Prefer small project-owned VectorDrawables or Compose vector paths over adding a large icon dependency. Do not use literal text glyphs such as `⋮`, `›`, or `×` as production icons.

### Asset acceptance

The lighthouse mark and hero art should be compared directly against the supplied raster. A generic lighthouse icon is not sufficient.

Do **not** crop and ship the entire mockup or any phone-frame/text composite as an application asset.

## 9. Personal screen — exact composition target

Rebuild `PersonalProfileScreen` around the left phone in the raster.

### 9.1 Header

Visual:

- transparent/deep background, no generic Material app-bar surface;
- left: Direction 2 lighthouse mark + `Harbor`;
- right: rounded dark-teal icon button with cyan shield/check treatment;
- match vertical alignment and margins in the raster.

Behavior mapping:

- the right icon may open a presentation-only privacy/security information sheet or an existing safe secondary action;
- do not attach new policy semantics to it.

### 9.2 Ready hero

Visual hierarchy must match the raster:

- large rounded teal/dark hero occupying the first major viewport region;
- lighthouse/beam illustration anchored to the right and lower area;
- shield/check visual near upper-left;
- title split naturally as `Work space` / `is ready`;
- body below;
- one full-width cyan `Open Work` CTA near the lower part of the hero;
- right-arrow icon inside the CTA;
- cyan button text/icon uses `HarborAccentDark`;
- no small Material status pill sitting above the title in the final ready-state design unless it is visually integrated exactly like the reference.

Behavior:

- `Open Work` invokes existing `onOpenWorkHarbor`.

### 9.3 Quick actions under CTA

Use the same two-column tile geometry as the raster.

Ready state:

- tile 1: `Send files` → existing `onSendFilesToWork`;
- tile 2: map to an existing safe secondary action (recommended `Advanced`) while retaining the same visual tile size and hierarchy.

Setup state:

- where no Harbor Work profile exists and Android allows provisioning, tile 2 may be `Create Work space` → existing `onProvision`.

Do not expose a duplicate primary Work-create action after Work is already ready.

### 9.4 Privacy card

Match the raster structure, not the current simple stacked text:

- one large dark rounded card;
- header row with lock icon, `Privacy by default`, and short muted supporting line;
- three separated rows;
- each row has a dedicated leading icon;
- title + detail text;
- trailing cyan confirmation/check treatment;
- subtle dividers;
- identical vertical rhythm.

Copy:

1. `No network permission`
   `Harbor itself cannot access the internet.`
2. `No analytics`
   `Harbor does not collect usage or telemetry.`
3. `Local only`
   `Harbor's app catalog and diagnostics stay on this device.`

### 9.5 Bottom navigation shell

Reproduce the mockup's rounded bottom navigation visually.

- `Personal` active and cyan;
- `Work` uses the existing cross-profile `onOpenWorkHarbor` callback;
- `Settings` must remain a presentation-only local surface; do not create a role/capability model.

A Personal settings sheet/surface may contain only already-existing UI-level secondary content/actions such as:

- Advanced entry using the existing callback/confirmation path;
- Update Harbor guidance;
- privacy/about information;
- Work-profile removal guidance.

Do not put Work DPC operations into Personal Settings.

### 9.6 Existing secondary content

The current `Your spaces`, `Update Harbor in Work`, additional-workspace controls, messages, and removal guidance must remain accessible, but they should not destroy the first-viewport fidelity.

Move them below the mockup-equivalent primary content or into the presentation-only Settings/Advanced surfaces while preserving existing callbacks and behavior.

The first normal ready-state viewport should look like the mockup, not like a stack of generic information cards.

## 10. Personal state variants

Use the **same hero artwork/component family** for all states so the visual identity does not collapse outside the happy path.

### Harbor ready

- teal hero;
- shield/check positive visual;
- `Work space is ready`;
- primary `Open Work`;
- Send files available.

### Ready to create

- same composition;
- title `Set up your Work space`;
- body from existing presentation mapping;
- primary `Create Work space`;
- hero may use a plus/work variation rather than ready check.

### Foreign/blocked

- same composition and dimensions;
- warning accent rather than cyan-positive;
- no unsafe create action when provisioning is not allowed;
- keep exact existing provisioning semantics.

## 11. Work-profile screen — exact composition target

Rebuild `WorkProfileScreen` around the right phone in the supplied raster.

This is the UI shown when Harbor runs inside the managed Work profile. Existing Work callbacks/controllers remain unchanged.

### 11.1 Work header

Match the raster:

- deep background with no generic raised app bar;
- left menu/hamburger icon;
- small Direction 2 Work mark;
- `Work space` title;
- compact second line with accurate Harbor text;
- right three-dot overflow.

Recommended accurate subtitle preserving the mockup rhythm:

`Managed by Harbor · Local catalog`

Do not use the raster's `No internet` claim.

Behavior mapping:

- left menu may open a lightweight Work navigation sheet with existing `Open Personal Harbor` and `Advanced` actions;
- right overflow opens the existing Work controls surface containing file sharing, APK installation, and Android Work settings;
- no new role resolver or policy model.

### 11.2 Search field

Do not use the default outlined Material text field appearance.

Match the raster:

- filled dark rounded search capsule;
- no visible outline in resting state;
- search icon at left;
- placeholder `Search apps`;
- sliders/filter-style icon at right for visual balance.

If no real filter behavior exists, the right icon may be disabled/informational or open a presentation-only local filter UI only if it can be implemented without changing app-management semantics. Do not invent persisted filtering state just to satisfy the icon.

### 11.3 Count / Select row

- small count aligned left;
- `Select` aligned right in cyan/teal;
- compact vertical spacing matching the raster;
- no large `HarborSectionTitle` block.

### 11.4 App rows

Match the right-phone list:

- approximately 80dp high;
- dark raised surface;
- `18.dp` radius;
- real app icon at 48dp;
- app label prominent;
- data-backed secondary line below label;
- status pill aligned to the right;
- true overflow icon at far right;
- subtle separation between rows.

Do not invent descriptions such as `Private messenger` or `Web browser`.

Use existing data for the secondary line, preferably the package name in a muted style. If this makes the row too busy, omit the secondary line while preserving vertical geometry; do not fabricate metadata.

Status pills:

- `Available`: dark green-teal pill with green/cyan text;
- `Frozen`: dark blue pill with icy blue icon/text;
- `Read-only`: neutral/muted treatment;
- `Disabled`: warning treatment.

### 11.5 Selection mode

Preserve existing selection behavior and pure helper rules.

Visually, selection mode may replace the header/count controls with a compact contextual bar, but it must remain within the Direction 2 design language:

- icon-based close action, not the text glyph `×`;
- selected count;
- compact Freeze/Unfreeze actions;
- system-app tap remains a no-op;
- system checkbox remains disabled;
- select-all-visible semantics unchanged.

## 12. App action sheet — match the mockup

The current generic `HarborSettingsRow` sheet is not visually sufficient.

Create a dedicated Direction 2 app-action sheet.

### 12.1 Sheet shell

- `HarborSheet` background;
- large rounded top corners (`~28.dp`);
- correct drag handle;
- height and vertical position similar to the raster;
- subtle divider lines;
- no repeated rounded card around every action.

### 12.2 App identity header

At top of the sheet:

- real app icon (`52–56.dp`);
- app label;
- data-backed secondary text (package name or current status);
- spacing matching the raster.

### 12.3 Action rows

Each action row uses:

- dedicated leading icon;
- label;
- optional supporting text only where the reference has it/useful;
- optional trailing affordance;
- divider between actions;
- no universal chevron.

Actions and existing callbacks:

- Open → existing launch callback;
- Freeze / Unfreeze → existing ViewModel/controller;
- Add to launcher / Add & unfreeze → existing shortcut callback;
- App details → existing details callback;
- Uninstall → existing Android uninstall callback.

### 12.4 Freeze switch

To reproduce the raster, the Freeze action may use a trailing switch.

Requirements:

- switch checked state derives solely from `app.isHidden`;
- disabled while operation is busy;
- user change invokes existing `setApplicationHidden` path;
- no remembered local checked state;
- no optimistic UI flip before authoritative state update.

### 12.5 Destructive action

`Uninstall` must visually match the raster:

- red trash icon;
- red text using `HarborDanger`;
- separated from normal actions;
- no generic neutral styling.

System apps continue to omit mutation/uninstall actions according to existing behavior.

## 13. Work controls and profile navigation mapping

The reference shows menu/overflow chrome rather than the current visible `Advanced` text button.

Use the existing callbacks in visually matching surfaces:

### Left menu / Work navigation sheet

Presentation-only options:

- `Open Personal Harbor` → existing `onOpenPersonalHarbor`;
- `Advanced` → existing `onAdvanced`.

### Right overflow / Work controls sheet

Keep existing Work-side behavior:

- file sharing → existing `controller.allowPersonalFileSharing()`;
- APK installs → existing `controller.allowApkInstalls()`;
- Android Work settings → existing `onOpenSystemSettings`;
- file-sharing disclosure text remains accurate.

Restyle this sheet into the same Direction 2 sheet language. Do not create Personal-side ownership of these operations.

## 14. Reusable Direction 2 component set

Refactor `ui/designsystem` toward components that can actually reproduce the design.

Recommended component shape:

```text
ui/designsystem/
  HarborVisualTokens.kt
  HarborBrandMark.kt
  HarborDirection2Hero.kt
  HarborDirection2BottomBar.kt
  HarborPrivacyPanel.kt
  HarborSearchField.kt
  HarborAppRow.kt
  HarborStatusPill.kt
  HarborActionSheet.kt
  HarborActionRow.kt
  HarborQuickActionTile.kt
```

Names may differ, but responsibilities should be separated.

The library remains presentation-only:

- no DPM;
- no profile-owner logic;
- no topology;
- no package operations;
- no file/APK policy calls;
- no Shizuku;
- no persistence/networking.

Screens pass resolved state/data/callbacks into components.

## 15. Theme changes

Update `HarborTheme.kt` and/or visual token definitions so the reference colors are explicit rather than incidental Material defaults.

Dark mode should use the Direction 2 palette directly.

Light mode should remain usable and coherent, but do not compromise dark-mode fidelity to force a single default Material palette.

No network/branding dependency is needed for theme work.

## 16. Expected file changes

At minimum expect changes to:

```text
app/src/main/java/com/monstera/harbor/ui/PersonalProfileScreen.kt
app/src/main/java/com/monstera/harbor/ui/WorkProfileScreen.kt
app/src/main/java/com/monstera/harbor/ui/designsystem/*
app/src/main/java/com/monstera/harbor/ui/theme/HarborTheme.kt
app/src/main/java/com/monstera/harbor/ui/privacy/PrivacyPresentation.kt  # copy/state only if needed
app/src/main/res/drawable/*                                            # Direction 2 vectors/art
```

Potential new drawable assets:

```text
harbor_direction2_mark.xml
harbor_direction2_work_mark.xml
harbor_lighthouse_hero.webp or .xml
ic_harbor_shield_check.xml
ic_harbor_send.xml
ic_harbor_workspace.xml
ic_harbor_privacy_lock.xml
ic_harbor_no_network.xml
ic_harbor_analytics.xml
ic_harbor_local.xml
ic_harbor_search.xml
ic_harbor_filter.xml
ic_harbor_menu.xml
ic_harbor_overflow.xml
ic_harbor_open.xml
ic_harbor_freeze.xml
ic_harbor_shortcut.xml
ic_harbor_details.xml
ic_harbor_uninstall.xml
ic_harbor_personal.xml
ic_harbor_work.xml
ic_harbor_settings.xml
```

Avoid changing core/policy/topology modules for visual fidelity.

## 17. Implementation phases

### Phase 0 — lock the baseline

1. Confirm branch head and current CI.
2. Keep current physical screenshots as the **before** images.
3. Ensure the raster reference is available locally to Codex.
4. Create Personal and Work reference crops from the raster for easier side-by-side comparison.
5. Do not use the simplified SVG as the visual target.

### Phase 1 — visual tokens + assets

1. Add explicit Direction 2 palette/tokens.
2. Add the lighthouse brand mark.
3. Add the hero lighthouse/beam/waves artwork.
4. Add the required small vector icon set.
5. Add reusable purpose-built surfaces/search/action-row primitives.
6. Render a small preview/sample where useful before changing both screens.

Gate: assets and colors must already look like the reference before screen migration continues.

### Phase 2 — Personal pixel-fidelity pass

1. Replace generic TopAppBar with custom branded header.
2. Replace current `HarborHeroCard` with the reference hero composition.
3. Implement full-width cyan CTA.
4. Implement quick-action tiles.
5. Implement structured privacy panel.
6. Implement mockup-style bottom navigation with cross-profile semantics preserved.
7. Move secondary content out of the first visual viewport without removing it.
8. Preserve all existing state variants/callbacks.

Gate: capture Personal screenshot on S24 and compare with left-phone reference before proceeding.

### Phase 3 — Work header/search/list fidelity

1. Replace generic TopAppBar.
2. Implement menu + Work mark + title/subtitle + overflow.
3. Replace OutlinedTextField with filled rounded Direction 2 search field.
4. Compact count/Select row.
5. Implement exact-style app rows/status pills/overflow.
6. Preserve search and selection semantics.

Gate: Work list screenshot should match the right-phone upper half before implementing the sheet.

### Phase 4 — Work app action sheet fidelity

1. Implement custom sheet shell.
2. Add app identity header with icon.
3. Add dedicated action icons and dividers.
4. Add authoritative Freeze switch treatment.
5. Add red destructive Uninstall row.
6. Preserve action availability rules and callbacks.

Gate: capture the sheet open on a normal user app and compare directly with the right-phone lower half.

### Phase 5 — secondary sheets and state variants

1. Restyle Work controls sheet.
2. Implement menu/profile navigation presentation using existing callbacks only.
3. Verify Personal setup/blocked variants use the same visual family.
4. Verify system/frozen/disabled/non-launchable app states.

### Phase 6 — accessibility + responsive behavior

Verify:

- 48dp touch targets;
- TalkBack descriptions for all icon-only controls;
- status meaning never color-only;
- 200% font scale preserves primary actions and scrollability;
- long app labels/package names truncate safely;
- sheet scrolls on short devices;
- content respects status/navigation insets;
- no nested-click ambiguity.

When accessibility requires a small visual departure, preserve the reference hierarchy and document the reason.

### Phase 7 — physical visual-diff loop

Primary device: Samsung Galaxy S24 / Android 16.

For both Personal and Work:

1. Force dark mode.
2. Capture current build screenshot at consistent device resolution.
3. Place beside the supplied reference crop.
4. Also create a 50% opacity overlay or image-diff view locally.
5. Adjust spacing, colors, sizes, typography, and icon placement.
6. Repeat until major visual discrepancies are gone.

Do not replace repository release screenshots until this visual review is accepted.

### Phase 8 — final screenshots and repository validation

After visual acceptance:

- capture real Personal screenshot;
- capture real Work list screenshot;
- capture an additional Work action-sheet screenshot for review even if README only uses two images;
- update docs/Fastlane screenshots as appropriate;
- run the full repository checks.

## 18. Test requirements

### Existing behavioral tests

All existing tests remain green.

Do not rewrite behavior tests merely to accommodate a visual refactor.

### Presentation tests

Keep existing pure mapping tests for:

- setup state;
- app status;
- selection-mode click intent;
- filtered count;
- Add & unfreeze copy.

Add pure tests only for meaningful presentation mapping. Do not unit-test pixel constants unnecessarily.

### Compose/UI tests

Where stable, test:

- Personal ready hero contains `Open Work`;
- setup hero contains `Create Work space` only when allowed;
- Work navigation item invokes cross-profile callback rather than local tab state;
- system apps remain non-selectable;
- action sheet hides invalid mutation actions;
- Freeze switch is bound to app hidden state and disabled while busy;
- Uninstall action is present only for non-system apps;
- privacy copy uses `No network permission`, not the false Work-offline claim.

## 19. Visual review checklist

### Personal

- [ ] Lighthouse Harbor mark matches reference.
- [ ] Header spacing matches reference.
- [ ] Hero occupies the same visual proportion.
- [ ] Lighthouse/beam/waves art is present and convincing.
- [ ] Hero title scale/line break matches.
- [ ] Cyan CTA is full width and correctly rounded.
- [ ] Quick-action tiles match size/surface treatment.
- [ ] Privacy card uses icons, dividers, details, trailing checks.
- [ ] Bottom navigation looks like reference.
- [ ] No generic stacked Material-card appearance remains in first viewport.

### Work

- [ ] Header/menu/work-mark/title/subtitle/overflow match reference.
- [ ] Search field is filled/rounded rather than outlined.
- [ ] Count/Select spacing matches.
- [ ] App rows are dense and reference-like.
- [ ] Status pills match reference palette/size.
- [ ] Real overflow icon replaces text glyph.
- [ ] Action sheet has app icon/header.
- [ ] Action rows have dedicated icons and dividers.
- [ ] Freeze uses authoritative switch treatment.
- [ ] Uninstall is visibly destructive/red.
- [ ] Sheet top radius/handle/background match reference.

## 20. Definition of done

PR #6 is visually complete only when all of the following are true:

- the supplied raster, not the simplified SVG, was used as the visual target;
- Personal first viewport closely reproduces the left-phone mockup;
- Work list closely reproduces the right-phone upper mockup;
- Work app action sheet closely reproduces the right-phone lower mockup;
- lighthouse logo/hero imagery is implemented rather than replaced by generic Material icons/cards;
- sampled colors and layered dark surfaces are visibly matched;
- generic `TopAppBar`, outlined search, generic card stack, text-glyph overflow, and universal-chevron action styling no longer define the primary UI;
- the few required semantic corrections are preserved;
- existing Harbor behavior/policy/profile ownership is unchanged;
- S24 dark-mode side-by-side visual review is accepted;
- light mode, large font, and TalkBack remain usable;
- full unit/lint/release/SBOM/prohibited-permission/reproducibility checks pass;
- final repository screenshots come from the accepted real-device build.

## 21. Final Codex self-review

Before declaring completion, answer these questions from the actual screenshots, not from the source code:

1. If the text were blurred, would the Harbor Personal screen still look like the supplied mockup?
2. Is the lighthouse identity obvious in the header and hero?
3. Is the hero visually dominant in the same way as the reference?
4. Does the Personal screen still look like generic Material cards? If yes, the pass is not done.
5. Does the Work search/list look as dense and layered as the reference?
6. Does each Work app action have the same icon-driven hierarchy as the reference?
7. Is Uninstall immediately distinguishable as destructive?
8. Is the Freeze switch authoritative rather than optimistic?
9. Did any visual shortcut introduce new policy, profile, or navigation semantics?
10. Did any invented app description or false privacy claim slip in?
11. Are the actual S24 screenshots close enough to the reference that a reviewer would describe them as the same design rather than merely the same information architecture?

If #11 is no, continue the visual iteration before refreshing release screenshots or moving the PR out of draft.

## 22. Minimal Codex execution prompt

```text
Continue PR #6 on branch codex/ui-privacy-dashboard.

The next pass is a UI-only pixel-fidelity implementation of the supplied
Direction 2 raster mockup. Read docs/design/PR6_DIRECTION2_PIXEL_FIDELITY_PLAN.md
first and treat it as authoritative over the earlier cleanup plans and the
simplified SVG.

Keep all current Harbor policy/profile/controller behavior unchanged. Rebuild
the Personal and Work presentation so the actual Samsung Galaxy S24 dark-mode
screens closely reproduce the raster: lighthouse branding and hero art, exact
dark/teal palette, full-width cyan CTA, quick-action tiles, structured privacy
panel, branded Work header, filled rounded search, dense app rows, status pills,
and icon-driven app action bottom sheet with red Uninstall and an authoritative
Freeze switch.

Do not call the work complete until real-device screenshots have been compared
side-by-side with the supplied raster and the major visual discrepancies are
gone.
```
