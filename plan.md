# Harbor remediation plan for Codex

## Goal

Resolve the issues found in the repository review without changing Harbor's product direction or weakening its public-API-first security model. Keep the core work-profile path independent from Shizuku and preserve the existing module boundaries.

## Ground rules

- Keep `app`, `core:data`, `core:policy`, `core:topology`, `feature:advanced`, and `privileged:shizuku` as separate modules.
- Do not introduce hidden Android APIs, reflection-based user-ID extraction, root-only behavior, network access, telemetry, or arbitrary shell execution.
- Prefer explicit failure over guessing profile/user relationships.
- Any privileged operation must validate its target against a fresh Shizuku-derived user list immediately before execution.
- Keep all commands as executable + argument arrays. Never concatenate shell command strings.
- Add regression coverage for each behavioral fix where practical.
- Run `./gradlew --no-daemon testDebugUnitTest lintDebug assembleRelease generateSbom` before considering the work complete.

## Priority 0 — restore trustworthy verification

### P0.1 Fix GitHub Actions branch trigger

File:
- `.github/workflows/android.yml`

Current problem:
- The repository default branch is `harbor`, while push CI listens only to `main`.

Required change:
- Trigger the Android workflow on pushes to `harbor`.
- Keep `pull_request` verification enabled.
- Do not otherwise weaken the current test, lint, release, SBOM, manifest, or reproducibility jobs.

Acceptance criteria:
- A direct push to `harbor` creates an Android workflow run.
- Pull requests continue to run the same verification.

## Priority 1 — core work-profile correctness

### P1.1 Correct `setApplicationHidden` semantics

Files:
- `core/policy/src/main/java/com/monstera/harbor/core/policy/WorkProfileController.kt`
- `core/policy/src/main/java/com/monstera/harbor/core/policy/AndroidWorkProfileController.kt`
- `app/src/main/java/com/monstera/harbor/ui/WorkProfileScreen.kt`
- new tests under `core/policy/src/test/...` if an Android-free seam is introduced

Problem:
- `DevicePolicyManager.setApplicationHidden()` returns whether the policy update succeeded, not the resulting hidden state.
- The UI currently stores the returned boolean as `ManagedApp.isHidden`.

Preferred implementation:
1. Change `setApplicationHidden` to return `PolicyResult<Unit>` rather than `PolicyResult<Boolean>`.
2. Treat a `false` return from `DevicePolicyManager.setApplicationHidden()` as a failed policy change.
3. On success, update local UI state using the requested `hidden` value or re-read `isApplicationHidden()` before updating UI.
4. Preserve protected-package handling.

Acceptance criteria:
- Successful Freeze produces `isHidden = true`.
- Successful Unfreeze produces `isHidden = false`.
- A platform `false` result is surfaced as a failure and does not silently change UI state.
- Exceptions remain converted to `PolicyResult.Failure`.

### P1.2 Remove `UserHandle.hashCode()` as an authoritative Android user ID

Files:
- `core/topology/src/main/java/com/monstera/harbor/core/topology/ProfileTopology.kt`
- topology models as needed
- `feature/advanced/src/main/java/com/monstera/harbor/feature/advanced/AdvancedScreen.kt`
- `privileged/shizuku/src/main/java/com/monstera/harbor/privileged/shizuku/ShizukuPrivilegedBackend.kt`

Problem:
- `UserHandle.hashCode()` is not a public contract for retrieving an Android user ID.
- Those candidate values can reach privileged `--user` commands.

Required architecture:
- Public-API topology may describe local relationships without claiming an authoritative numeric user ID.
- Numeric IDs used for Shizuku commands must come from Shizuku/system command output (`cmd user list` or equivalent already parsed by `SystemUserParser`).
- Resolve a privileged target from fresh privileged topology before every privileged operation.
- If Harbor cannot map a local profile to an authoritative privileged user with confidence, disable the operation and explain why.

Implementation suggestion:
- Replace mandatory `AssociatedProfile.userId: AndroidUserId` with a non-authoritative/candidate field or no ID at all.
- Add a privileged topology/resolution function that returns authoritative `SystemUser` targets.
- Keep the public-API detector useful for UI state, ownership and profile-kind detection.

Acceptance criteria:
- No Shizuku command target originates from `UserHandle.hashCode()`.
- Search for `hashCode()` in topology code shows no use as a privileged user identifier.
- Advanced operations fail closed if mapping is ambiguous.

### P1.3 Resolve parent/work/sibling profiles explicitly

Files:
- `core/topology/...`
- `feature/advanced/.../AdvancedScreen.kt`
- `privileged/shizuku/.../ShizukuPrivilegedBackend.kt`

Problem:
- `ClonePanel` currently selects the first associated non-current profile as the source user.
- Devices may expose more than a simple personal + work pair, including Private Space or sibling profiles.

Required behavior:
- Never infer the parent as `firstOrNull { !isCurrent }`.
- Resolve the actual parent/full-user relationship using authoritative privileged user metadata where possible.
- Permit `install-existing` cloning only when the source full user and target managed profile relationship is unambiguous.
- Otherwise show a non-destructive unsupported/ambiguous state.

Acceptance criteria:
- A topology with personal + work + another profile does not accidentally choose the sibling profile.
- Tests cover at least a simple pair and an ambiguous multi-profile case.

### P1.4 Correct full-user/switchable-user classification

Files:
- `core/topology/src/main/java/com/monstera/harbor/core/topology/PrivilegedModels.kt`
- `core/topology/src/test/java/com/monstera/harbor/core/topology/SystemUserParserTest.kt`

Problem:
- `isSwitchableFullUser` currently rejects all users carrying `FLAG_SYSTEM`, which can incorrectly reject user 0 on normal Android configurations.

Required change:
- Model full/profile/restricted/disabled/system flags accurately.
- Distinguish a full user that also has `FLAG_SYSTEM` from a headless system user.
- Base switchability on full-user semantics and known non-switchable flags rather than `FLAG_SYSTEM` alone.

Acceptance criteria:
- Tests cover:
  - normal owner/full user 0;
  - secondary full user;
  - managed profile;
  - restricted/disabled user;
  - headless system user if identifiable from available flags/output.

## Priority 2 — privileged-process reliability

### P2.1 Drain command output after the capture limit

File:
- `privileged/shizuku/src/main/java/com/monstera/harbor/privileged/shizuku/HarborUserService.kt`

Problem:
- Output capture stops reading once `MAX_OUTPUT_CHARS` is reached.
- A child process that continues writing can block on a full pipe.

Required change:
- Continue draining stdout/stderr until EOF even after Harbor stops storing output.
- Store at most `MAX_OUTPUT_CHARS` characters.
- Preserve the 30-second command timeout and forced termination behavior.
- Optionally mark output as truncated in `CommandResponse` or append a bounded truncation marker.

Acceptance criteria:
- Large command output cannot deadlock because Harbor stopped reading.
- Stored output remains bounded.
- Timeout behavior still returns exit code 124.

### P2.2 Strengthen parser coverage for real OEM/AOSP output

Files:
- `core/topology/src/main/java/com/monstera/harbor/core/topology/SystemUserParser.kt`
- tests in `core/topology/src/test/...`

Tasks:
- Add representative `cmd user list` samples from tested Android versions/OEMs when available.
- Fail safely on malformed/unrecognized lines.
- Do not infer destructive capabilities from partially parsed output.

## Priority 3 — UX correctness and safety

### P3.1 Fix misleading Work settings action

File:
- `app/src/main/java/com/monstera/harbor/MainActivity.kt`
- possibly `WorkProfileScreen.kt` strings

Problem:
- `Settings.ACTION_SYNC_SETTINGS` opens account sync settings, not a guaranteed work-profile settings surface.

Required change:
- Stop presenting that intent as a guaranteed Work Profile settings destination.
- Use the most appropriate supported settings intent available for the intended action, with a generic Settings fallback.
- Rename the button if Android cannot reliably deep-link to a dedicated work-profile page across supported versions/OEMs.

Acceptance criteria:
- The UI label accurately describes where the user will be sent.
- No unsupported/private intent is introduced.

### P3.2 Add a way to disable Advanced tools

Files:
- `feature/advanced/src/main/java/com/monstera/harbor/feature/advanced/AdvancedScreen.kt`
- `app/src/main/java/com/monstera/harbor/ui/HarborRoot.kt`
- `core/data/src/main/java/com/monstera/harbor/core/data/HarborPreferences.kt`

Required behavior:
- Provide an explicit UI action to disable Advanced tools.
- On disable, persist `advanced_tools_enabled = false`, release Shizuku bindings, and return to the core UI.
- Do not revoke Shizuku globally; Harbor should only release its own connection/state.

## Priority 4 — test and CI hardening

### P4.1 Add policy regression tests

Goal:
- Make the freeze/unfreeze semantic bug impossible to reintroduce.

Approach:
- If direct JVM testing of `DevicePolicyManager` is awkward, introduce a minimal internal adapter around the calls Harbor needs. Do not introduce a large framework or dependency injection library.
- Unit-test Harbor's interpretation of platform return values.

### P4.2 Decide how instrumentation tests are validated

Current state:
- `app/src/androidTest/.../ManifestSecurityTest.kt` exists, but the normal CI job does not run connected instrumentation tests.

Required decision:
- Either add an emulator/instrumentation CI job, or move security assertions that do not need a device into host-side verification/scripts.
- Avoid making the main PR loop unnecessarily slow unless the instrumentation coverage provides unique value.

### P4.3 Pin GitHub Actions by commit SHA

File:
- `.github/workflows/android.yml`

Task:
- Pin third-party actions (`actions/checkout`, `actions/setup-java`, `android-actions/setup-android`, `gradle/actions/setup-gradle`, `actions/upload-artifact`) to reviewed commit SHAs.
- Keep a comment with the human-readable release version next to each SHA for maintainability.

## Suggested execution order for Codex

1. Fix CI branch trigger.
2. Fix `setApplicationHidden` contract + regression coverage.
3. Refactor user/profile ID handling so privileged operations use authoritative Shizuku-derived IDs only.
4. Add explicit profile relationship resolution and fail-closed ambiguity handling.
5. Correct full-user classification and tests.
6. Fix privileged output draining.
7. Correct Settings UX.
8. Add Advanced disable action.
9. Harden remaining CI/tests/action pinning.
10. Run full verification and update `docs/COMPATIBILITY.md` with any newly verified behavior.

## Files Codex should inspect before editing

- `README.md`
- `docs/ARCHITECTURE.md`
- `docs/THREAT_MODEL.md`
- `docs/COMPATIBILITY.md`
- `.github/workflows/android.yml`
- `app/src/main/java/com/monstera/harbor/MainActivity.kt`
- `app/src/main/java/com/monstera/harbor/ui/HarborRoot.kt`
- `app/src/main/java/com/monstera/harbor/ui/WorkProfileScreen.kt`
- `core/policy/src/main/java/com/monstera/harbor/core/policy/WorkProfileController.kt`
- `core/policy/src/main/java/com/monstera/harbor/core/policy/AndroidWorkProfileController.kt`
- `core/topology/src/main/java/com/monstera/harbor/core/topology/ProfileTopology.kt`
- `core/topology/src/main/java/com/monstera/harbor/core/topology/PrivilegedModels.kt`
- `core/topology/src/main/java/com/monstera/harbor/core/topology/SystemUserParser.kt`
- `feature/advanced/src/main/java/com/monstera/harbor/feature/advanced/AdvancedScreen.kt`
- `privileged/shizuku/src/main/java/com/monstera/harbor/privileged/shizuku/HarborUserService.kt`
- `privileged/shizuku/src/main/java/com/monstera/harbor/privileged/shizuku/ShizukuPrivilegedBackend.kt`

## Definition of done

- All P0 and P1 items are implemented before widening alpha testing.
- No privileged command uses an unverified/candidate user ID.
- Freeze and Unfreeze display the correct state after success and failures remain visible.
- Multi-profile ambiguity fails closed.
- Large privileged output cannot block the child process because of Harbor's capture limit.
- CI runs on the real default branch and PRs.
- Existing privacy/security constraints remain intact.
- Unit/lint/release/SBOM/manifest/reproducibility verification passes.
- `docs/THREAT_MODEL.md` and `docs/COMPATIBILITY.md` are updated if implementation details or residual risks change.
