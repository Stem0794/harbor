# Codex: start here

Implement the Harbor **Privacy Dashboard** UI refactor on this branch. Do not merge the scaffold as the finished feature.

Read these files in this order before editing code:

1. `docs/design/UI_PRIVACY_DASHBOARD_PLAN.md`
2. `docs/design/PRIVACY_DASHBOARD_REVIEW.md`
3. `docs/design/UI_COMPONENT_LIBRARY.md`
4. `docs/design/CODEX_PRIVACY_DASHBOARD_TASK.md`
5. `docs/design/harbor-ui-direction2-privacy-dashboard.svg`
6. the current live UI files referenced by the plan

The plan defines the target design. The review records semantic corrections found after comparing the mockup against Harbor's real architecture. The UI component guide defines the reusable visual-library boundary. The task file gives the implementation order and acceptance criteria. If the visual reference conflicts with the plan/review, **the plan/review wins**.

## Critical rules to keep in mind

- Harbor itself has no network permission. Do **not** claim that apps inside the Work profile have no Internet access.
- Personal and Work Harbor are separate app instances in separate Android profiles. Do not turn them into fake same-process tabs.
- File-sharing and APK-install policy controls are profile-owner/DPC operations. They are actionable only from the **Work Harbor instance**. Personal Settings may route the user to Work, but must not call those policy operations locally.
- Keep Advanced/Shizuku optional and explicitly opt-in.
- Do not use optimistic UI for freeze/unfreeze, sharing-policy, or APK-policy changes. Controller results remain authoritative.
- Preserve existing per-app action availability. The pure mapper/tests in `ui/privacy` are the regression baseline for the card-to-bottom-sheet migration.
- Keep generic visual primitives in `ui/designsystem` and keep policy/topology/feature semantics out of that package.
- Reuse or extend the Harbor UI component library instead of creating parallel card/row/status implementations for future screens.
- Do not invent app descriptions, privacy ratings, network state, storage use, or version fields that Harbor's current catalog does not provide.
- Do not add permissions, network dependencies, telemetry, hidden APIs, general shell access, or destructive user deletion.

## Implementation sequence

1. Run the baseline verification commands from the task file.
2. Refactor Personal dashboard using the scaffolded setup-state resolver, privacy facts, and reusable Harbor components.
3. Refactor Work into the compact app-list-first layout and app action bottom sheet.
4. Move multi-selection into a contextual top bar while preserving batch semantics.
5. Add role-aware local Settings, with Work-only DPC policy controls enforced by actual local profile-owner state.
6. Visually align Advanced without changing its backend semantics.
7. Add targeted Compose tests and retain the pure presentation tests.
8. Update the UI component library guide if reusable patterns changed.
9. Run the full repository verification and perform the final review checklist.

The scaffold branch was reviewed before handoff and its code additions passed Harbor's GitHub Actions unit/lint, release build, SBOM, prohibited-permission, and reproducibility checks. Continue to keep CI green as implementation proceeds.
