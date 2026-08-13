# Codex task: implement Direction 2 visual fidelity on PR #6

Branch: `codex/ui-privacy-dashboard`
PR: #6
Scope: **UI-only visual-fidelity pass**

## Authoritative inputs

1. `docs/design/PR6_DIRECTION2_PIXEL_FIDELITY_PLAN.md`
2. user-supplied `harbor-direction2-reference.png` raster mockup
3. current Personal/Work runtime behavior and callbacks

The raster is the visual source of truth. The simplified SVG is not.

## Hard boundary

Preserve all existing:

- Personal/Work profile routing;
- provisioning behavior and Android consent;
- DevicePolicyManager/profile-owner behavior;
- callbacks/controllers/ViewModels;
- topology and user-ID trust rules;
- file-sharing behavior;
- APK-install behavior;
- launch/freeze/unfreeze/details/uninstall behavior;
- batch sequential/partial-failure behavior;
- shortcut behavior and signer validation;
- Advanced/Shizuku behavior;
- manifest permissions and dependency policy.

Do not add a role-aware Settings architecture, policy capability resolver, new policy state, new network capability, telemetry, hidden APIs, arbitrary shell access, or destructive user deletion.

## Implement in this order

### 1. Reference + before state

- Ensure the supplied raster is locally visible to the coding session.
- Keep the current S24 screenshots as before-images.
- Create convenient Personal/Work reference crops locally.
- Do not approximate from `harbor-ui-direction2-privacy-dashboard.svg`.

### 2. Direction 2 assets and tokens

- Add explicit sampled dark/teal color tokens from the fidelity plan.
- Add the lighthouse Harbor mark matching the raster.
- Add the Work mark matching the raster.
- Add lighthouse/beam/waves hero artwork.
- Add dedicated vector icons required by the mockup.
- Replace text glyph icons such as `⋮`, `›`, and `×`.
- Keep these resources presentation-only.

### 3. Personal screen

Rebuild the first viewport to match the left phone:

- branded Harbor header;
- large lighthouse hero;
- full-width cyan `Open Work` CTA;
- quick-action tiles;
- structured Privacy by default panel with icons/dividers/trailing checks;
- mockup-style bottom navigation shell;
- move current secondary information out of the first visual viewport without removing its behavior.

Use the semantic substitutions in the fidelity plan:

- `No network permission`, not a false Work-offline claim;
- no duplicate Work-profile creation when Work is already ready;
- `Work` navigation uses the existing cross-profile callback.

### 4. Work header/search/list

Rebuild the right-phone upper half:

- menu icon + Work mark + `Work space` + accurate subtitle + overflow;
- filled rounded search surface, not a default outlined field;
- compact count/Select row;
- dense rounded app rows with 48dp icons;
- data-backed secondary line only;
- reference-style status pills;
- real overflow icon.

Preserve current search, count, selection, and system-app rules.

### 5. App action bottom sheet

Rebuild the right-phone lower half:

- Direction 2 dark sheet shell;
- rounded top corners and drag handle;
- real app icon + app identity header;
- action-specific leading icons;
- dividers;
- Open;
- Freeze/Unfreeze with an authoritative trailing switch;
- Add to launcher / Add & unfreeze;
- App details;
- red Uninstall row.

The Freeze switch must derive from `app.isHidden` and must not update optimistically before controller success.

### 6. Secondary Work surfaces

- Left menu may present existing `Open Personal Harbor` and `Advanced` callbacks.
- Right overflow keeps existing Work-side file-sharing, APK-install, and Android Settings callbacks.
- Restyle these surfaces consistently without creating a new policy/settings architecture.

### 7. State/accessibility pass

Verify:

- ready/setup/blocked Personal hero variants;
- available/frozen/read-only/disabled/non-launchable Work rows;
- selection mode;
- 48dp touch targets;
- TalkBack labels;
- large font;
- long app names/package names;
- sheet scrolling/insets;
- no status communicated by color alone.

### 8. Physical visual-diff loop

On Samsung Galaxy S24 / Android 16:

1. force dark mode;
2. capture Personal screenshot;
3. compare side-by-side and with a transparency overlay against the Personal raster crop;
4. iterate colors, spacing, typography, art and dimensions;
5. capture Work list screenshot;
6. repeat against the right-phone upper crop;
7. open a normal user-app action sheet;
8. repeat against the right-phone lower crop.

Do not stop because the information architecture is similar. Stop only when the screenshots read as the **same design**.

### 9. Final validation

Run:

```shell
./gradlew --no-daemon testDebugUnitTest lintDebug assembleRelease generateSbom
```

Also run prohibited-permission verification, reproducibility verification, and diff/whitespace validation.

Refresh docs/Fastlane screenshots only after the new visual result is accepted.

## Definition of done

- Lighthouse logo/hero imagery matches the raster closely.
- Personal first viewport matches the left-phone composition.
- Work list matches the right-phone composition.
- Action sheet matches the right-phone sheet composition.
- Explicit sampled palette replaces generic Material-default appearance.
- No generic TopAppBar/outlined-search/generic-card-stack/text-glyph icon treatment dominates the primary UI.
- Accurate Harbor semantics replace only the few incorrect mockup claims/actions documented in the plan.
- Existing behavior/policy/profile ownership is unchanged.
- S24 side-by-side visual review is accepted.
- Accessibility remains usable.
- Full CI and reproducibility checks pass.

If the screen still looks like a generic Material reinterpretation of the mockup rather than the mockup itself, continue iterating.
