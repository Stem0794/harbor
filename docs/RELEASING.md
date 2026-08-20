# Release workflow

1. Confirm the production application ID and DPC receiver component are unchanged.
2. Update `versionCode`, `versionName`, the Fastlane changelog, and any affected compatibility notes.
3. Run unit tests, lint, release assembly, manifest permission audit, and `./gradlew generateSbom`; review the generated inventory against `DEPENDENCIES.md`.
4. Run `sh scripts/verify-deterministic-build.sh` as a same-environment deterministic-build smoke test. F-Droid's independent rebuild is the authoritative reproducibility check.
5. Test an update from the previous published Harbor release while Harbor remains profile owner.
6. Complete the physical-device release matrix.
7. Create an immutable annotated source tag named `v<versionName>` and verify it points to the intended release commit.
8. From a clean checkout of that exact tag, build the unsigned release APK with the documented JDK and Android SDK versions.
9. Sign the APK with `sh scripts/sign-release.sh`. The script uses an F-Droid-compatible Android Build Tools 34 `apksigner`, writes the exact asset name `app-release-signed.apk` by default, and verifies the resulting certificate before returning success.
10. Publish the GitHub release with `app-release-signed.apk` attached.
11. Verify that F-Droid detects the tag, independently rebuilds it, and successfully completes its reproducibility check before treating the F-Droid release as complete.

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

This constraint applies to the tool used to sign the final APK; Harbor may continue to compile against newer Android SDK and Build Tools versions. `scripts/sign-release.sh` and `scripts/verify-release-signing.sh` therefore resolve Build Tools `34.0.0/apksigner` from `ANDROID_SDK_ROOT` or `ANDROID_HOME` by default. `APKSIGNER_BIN` may be set explicitly when the compatible signer lives elsewhere. Re-check F-Droid's reproducible-build documentation before changing the signing-tool version.

## Release signing

The private release keystore and its passwords must stay outside the repository. Harbor's `.gitignore` excludes common keystore formats, APK outputs, `keystore.properties`, and the legacy local password-file name, but release secrets should still be stored in a dedicated password manager or secret store rather than inside the working tree.

Before signing, provide these environment variables through the release environment or secret manager:

- `HARBOR_RELEASE_KEYSTORE`: path to the Monstera release keystore.
- `HARBOR_RELEASE_KEY_ALIAS`: alias of the Harbor release key.
- `HARBOR_KEYSTORE_PASSWORD`: keystore password.
- `HARBOR_KEY_PASSWORD`: private-key password.

The signing script passes password references to `apksigner` using its `env:` input mode; it does not put password values in the command arguments.

From a clean checkout of the release tag:

```shell
./gradlew --no-daemon clean assembleRelease
sh scripts/verify-deterministic-build.sh
sh scripts/sign-release.sh
```

The default input and output are:

```text
app/build/outputs/apk/release/app-release-unsigned.apk
app-release-signed.apk
```

Custom input/output paths can be passed as the first and second arguments:

```shell
sh scripts/sign-release.sh path/to/unsigned.apk path/to/app-release-signed.apk
```

The signing script fails closed if the keystore, credentials, compatible signer, unsigned APK, or expected certificate is missing. It calls `scripts/verify-release-signing.sh` after signing, so a mismatched release certificate is rejected before upload.

The expected release certificate can be checked independently with:

```shell
sh scripts/verify-release-signing.sh app-release-signed.apk
```

For versions that F-Droid successfully reproduces and publishes through the configured upstream-signed flow, GitHub and F-Droid use the same application signing identity, so signature compatibility permits in-place updates between those sources subject to Android's normal version rules.
