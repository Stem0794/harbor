# Release workflow

1. Confirm the production application ID and DPC receiver component are unchanged.
2. Update `versionCode`, `versionName`, the Fastlane changelog, and any affected compatibility notes.
3. Run unit tests, lint, release assembly, manifest permission audit, and `./gradlew generateSbom`; review the generated inventory against `DEPENDENCIES.md`.
4. Build the unsigned release APK twice in clean environments and compare hashes.
5. Test an update from the previous F-Droid-signed release while Harbor remains profile owner.
6. Complete the physical-device release matrix.
7. Create and sign an immutable source tag.
8. Publish the GitHub release if a direct-testing APK is being provided.
9. Verify that F-Droid detects and builds the tagged release.

## F-Droid

Harbor is published through the official F-Droid repository. Descriptive store metadata, changelogs, screenshots, the app icon, and the feature graphic are maintained under `fastlane/metadata/android/` in this repository.

The canonical F-Droid build recipe is maintained in `fdroiddata`, not duplicated here. Release tags and version fields in this repository must remain compatible with F-Droid's update checks. If a release is not picked up as expected, inspect the current `fdroiddata` recipe and F-Droid build logs before changing Harbor's source metadata.

F-Droid rebuilds Harbor from source and signs the published APK with the repository signing key.

## GitHub signing key

GitHub alpha releases may contain a Monstera-signed APK for direct testing. The private keystore is never committed to the repository. Keep the keystore and its password in separate secure backups; losing either prevents future updates for installations signed with that key.

Do not treat the Monstera-signed GitHub APK as the F-Droid production artifact. The GitHub and F-Droid builds use different signing identities, so installations normally cannot update across those sources in place.
