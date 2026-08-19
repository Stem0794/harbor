# Release workflow

1. Confirm the production application ID and DPC receiver component are unchanged.
2. Update `versionCode`, `versionName`, the Fastlane changelog, and any affected compatibility notes.
3. Run unit tests, lint, release assembly, manifest permission audit, and `./gradlew generateSbom`; review the generated inventory against `DEPENDENCIES.md`.
4. Run `scripts/verify-reproducible.sh` as a same-environment deterministic-build smoke test. F-Droid's independent rebuild is the authoritative reproducibility check.
5. Test an update from the previous published Harbor release while Harbor remains profile owner.
6. Complete the physical-device release matrix.
7. Create and sign an immutable source tag named `v<versionName>` and verify it points to the intended release commit.
8. From a clean checkout of that exact tag, build the unsigned release APK with the documented JDK and Android SDK versions.
9. Sign the APK with the Monstera release key using an F-Droid-compatible `apksigner`, producing the exact asset name `app-release-signed.apk`.
10. Run `scripts/verify-release-signing.sh app-release-signed.apk` and confirm the expected signing certificate before upload.
11. Publish the GitHub release with `app-release-signed.apk` attached.
12. Verify that F-Droid detects the tag, independently rebuilds it, and successfully completes its reproducibility check before treating the F-Droid release as complete.

## F-Droid

Harbor is published through the official F-Droid repository. Descriptive store metadata, changelogs, screenshots, the app icon, and the feature graphic are maintained under `fastlane/metadata/android/` in this repository.

The canonical F-Droid build recipe is maintained in `fdroiddata`, not duplicated here. Harbor's current recipe uses F-Droid's reproducible upstream-signed APK flow: `Binaries` points to the signed GitHub release asset and `AllowedAPKSigningKeys` pins the expected Harbor signing certificate.

F-Droid independently rebuilds the tagged source. If that rebuild matches the downloaded upstream APK, F-Droid publishes the upstream developer-signed APK. If the reproducibility check fails, F-Droid skips that version rather than publishing a separately F-Droid-signed APK.

The release contract currently expected by `fdroiddata` is:

- tag: `v<versionName>`
- GitHub release asset: `app-release-signed.apk`
- allowed signing certificate SHA-256: `1f68efbefd07ea0c1aa7d79d9fd720c3dda74ac5524dcf398efea0d379b3494d`

Release tags, version fields, asset naming, and signing identity must remain compatible with that recipe. If a release is not picked up or does not reproduce, inspect the current `fdroiddata` recipe and F-Droid build/reproducibility logs before changing Harbor's source metadata.

### Signing-tool compatibility

F-Droid verifies upstream signatures using `apksigcopier`. As of August 2026, F-Droid documents an incompatibility with APKs signed by `apksigner` from Android Build Tools 35 and newer and recommends Build Tools 34 `apksigner` for reproducible upstream-signed APKs.

This constraint applies to the tool used to sign the final APK; Harbor may continue to compile against newer Android SDK and Build Tools versions. Set `APKSIGNER_BIN` explicitly to the intended Build Tools 34 `apksigner` when running the signing verification script. Re-check F-Droid's reproducible-build documentation before changing the signing-tool version.

## GitHub signing key

GitHub releases intended for F-Droid reproducibility use the Monstera release signing identity. The private keystore is never committed to the repository. Keep the keystore and its password in separate secure backups; losing either prevents future updates for installations signed with that key.

Before uploading a signed release APK, verify it with:

```shell
APKSIGNER_BIN=/path/to/android-sdk/build-tools/34.0.0/apksigner \
  scripts/verify-release-signing.sh app-release-signed.apk
```

For versions that F-Droid successfully reproduces and publishes through the configured upstream-signed flow, GitHub and F-Droid use the same application signing identity, so signature compatibility permits in-place updates between those sources subject to Android's normal version rules.
