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
