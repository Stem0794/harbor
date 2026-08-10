# Harbor

Harbor is a free and open-source Android work-profile manager. It creates a standard Android managed profile, then lets the user inspect, launch, freeze, unfreeze, and uninstall applications inside that profile.

The core application uses supported Android `DevicePolicyManager` APIs and works without Google Play services, network access, telemetry, root, or Shizuku. Optional Advanced tools integrate with a separately installed Shizuku service for diagnostics, package `install-existing`, and experimental multiple full-user workspaces.

## Project status

Harbor is pre-release software. The public-API MVP compiles, but profile provisioning, signed DPC upgrades, Shizuku commands, and OEM behavior still require the physical-device validation described in [the compatibility guide](docs/COMPATIBILITY.md).

Do not use a work profile containing irreplaceable data during development. Removing a work profile or full Android user permanently deletes its data.

## Supported design

- Android 10/API 29 through Android 16/API 36.
- One standard work profile per full Android user.
- Multiple workspaces through multiple full Android users on devices that expose multi-user support.
- No attempt to bypass Android's one-managed-profile-per-parent limit.
- No automated `dpm set-profile-owner`, hidden API bypass, direct `su`, arbitrary shell, or background telemetry.

## Build

Requirements:

- JDK 17
- Android SDK Platform 36
- Android Build Tools 36.0.0

```shell
./gradlew testDebugUnitTest lintDebug assembleRelease
./gradlew generateSbom
```

The production application ID is `io.github.theodorekonikowski.harbor`. Debug builds use `io.github.theodorekonikowski.harbor.debug` so development profiles cannot be confused with production profiles.

## Modules

- `app`: Compose UI, DPC receiver, provisioning, and application composition.
- `core:policy`: supported profile-owner operations.
- `core:data`: local application catalog and preferences.
- `core:topology`: identifiers, profile topology, and privileged interfaces.
- `privileged:shizuku`: the allowlisted Shizuku `UserService` implementation.
- `feature:advanced`: opt-in Shizuku and multiple-user screens.

See [Architecture](docs/ARCHITECTURE.md), [Dependency audit](docs/DEPENDENCIES.md), [Threat model](docs/THREAT_MODEL.md), and [Privacy](docs/PRIVACY.md).

## License

Apache License 2.0. Harbor is inspired by Island, but is a new implementation and does not use Island's legacy module or privileged-service architecture.
