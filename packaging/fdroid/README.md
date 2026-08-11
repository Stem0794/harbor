# F-Droid packaging

The production package is `com.monstera.harbor` and is intended to be built and signed by the official F-Droid repository.

The checked-in recipe is a candidate for an `fdroiddata` merge request. It is
not itself published by F-Droid until the corresponding metadata is accepted.

## Before submission

1. Keep the public source repository at its permanent URL.
2. Tag the audited source revision and make sure the recipe's `commit` and
   version fields match that tag.
3. Clone `https://gitlab.com/fdroid/fdroiddata`, copy this recipe to its
   `metadata/` directory, and run `fdroid readmeta`, `fdroid lint`,
   `fdroid checkupdates`, and `fdroid build -v -l`.
4. Run `fdroid scanner` and `fdroid build --server` where a configured F-Droid
   build server is available. The official CI will repeat these checks.
5. Open a merge request against `f-droid/fdroiddata:master` and complete the
   new-app checklist, including source availability, licensing, and author
   confirmation.
6. Ask reviewers whether the optional external Shizuku integration needs an
   anti-feature or additional disclosure. Do not hide the integration.
7. Verify the update path on an active profile-owner installation before a
   stable release.

## Signing and migration

F-Droid normally signs its build with an F-Droid repository key. The Monstera
keystore used for GitHub alpha APKs must never be committed or uploaded to
`fdroiddata`. A Monstera-signed installation generally cannot be updated by
the first F-Droid-signed APK, so testers should uninstall the GitHub alpha
before installing the official F-Droid build. Subsequent F-Droid updates will
use the F-Droid signing key.

F-Droid's upstream-signed reproducible-build flow is a separate, reviewer-
approved configuration and is not assumed here.
