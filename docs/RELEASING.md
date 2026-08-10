# Release workflow

1. Confirm the production application ID and DPC receiver component are unchanged.
2. Update `versionCode`, `versionName`, changelog, compatibility notes, and F-Droid metadata.
3. Run unit tests, lint, release assembly, manifest permission audit, and `./gradlew generateSbom`; review the generated inventory against `DEPENDENCIES.md`.
4. Build the unsigned release APK twice in clean environments and compare hashes.
5. Test an update from the previous F-Droid-signed candidate while Harbor remains profile owner.
6. Complete the physical-device release matrix.
7. Create and sign an immutable source tag.
8. Submit the pinned tag/commit to `fdroiddata` and retain its build log.

F-Droid is the production signing authority. Upstream debug artifacts have a different application ID and cannot update production installations.

## Alpha signing key

The `v0.1.0-alpha01` GitHub release also contains a Monstera-signed APK for direct alpha testing. The private keystore is never committed to the repository. Keep the keystore and its password in separate secure backups; losing either prevents future updates for installations signed with that key.

Do not treat the Monstera-signed alpha APK as the F-Droid production artifact. F-Droid normally signs its build with the repository signing key, so a Monstera-signed installation and an F-Droid-signed installation cannot update each other unless the signing arrangement is deliberately coordinated.
