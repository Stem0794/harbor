# Release workflow

1. Confirm the production application ID and DPC receiver component are unchanged.
2. Update `versionCode`, `versionName`, changelog, compatibility notes, and F-Droid metadata.
3. Run unit tests, lint, release assembly, manifest permission audit, and `./gradlew generateSbom`; review the generated inventory against `DEPENDENCIES.md`.
4. Build the unsigned release APK twice in clean environments and compare hashes.
5. Test an update from the previous F-Droid-signed candidate while Harbor remains profile owner.
6. Complete the physical-device release matrix.
7. Create and sign an immutable source tag.
8. Submit the pinned tag/commit to `fdroiddata` and retain its build log.

## Official F-Droid submission

F-Droid does not accept a GitHub APK upload as the source of truth. The
maintainer submits `packaging/fdroid/com.monstera.harbor.yml` to a fork of
`https://gitlab.com/fdroid/fdroiddata`, validates it with `fdroid readmeta`,
`fdroid lint`, `fdroid checkupdates`, and `fdroid build -v -l`, and opens a
merge request against `f-droid/fdroiddata:master`. F-Droid then rebuilds the
tagged source in its isolated build environment and signs the published APK.

The current recipe intentionally lists only the latest tested build. Older
alpha tags remain available in Git history but are not unnecessarily rebuilt
by the initial submission.

F-Droid is the production signing authority. Upstream debug artifacts have a different application ID and cannot update production installations.

## Alpha signing key

GitHub alpha releases may contain a Monstera-signed APK for direct testing. The private keystore is never committed to the repository. Keep the keystore and its password in separate secure backups; losing either prevents future updates for installations signed with that key.

Do not treat the Monstera-signed alpha APK as the F-Droid production artifact. F-Droid normally signs its build with the repository signing key, so a Monstera-signed installation and an F-Droid-signed installation cannot update each other unless the signing arrangement is deliberately coordinated.
