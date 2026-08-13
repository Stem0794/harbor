<p align="center">
  <img src="docs/branding/harbor-logo.svg" alt="Harbor lighthouse logo" width="128" />
</p>

<h1 align="center">Harbor</h1>

<p align="center"><strong>A private space for Android apps, built on Android's work-profile system.</strong></p>

Harbor is a free and open-source Android app that creates and manages a standard Android work profile. It keeps selected apps and their data separate from the personal side of the phone.

Harbor does **not** require Google Play services, root, a cloud account, analytics, advertising, or Internet access for its core features.

> **Harbor is alpha software.** Use it for testing and non-critical data while device compatibility is still being expanded.

## What Harbor does

- Creates a standard Android work profile.
- Shows and searches apps installed inside Work.
- Launches apps and opens Android app details.
- Freezes and unfreezes eligible apps.
- Uses Android's confirmation flow for uninstall.
- Creates launcher shortcuts for Work apps.
- Sends selected files from Personal to Work.
- Prepares APK installation while Android keeps its normal source-consent prompts.
- Optionally uses Shizuku for advanced diagnostics, package cloning, and experimental additional workspaces.

The normal Work-profile features do **not** require Shizuku.

## Screenshots

<p align="center">
  <img src="docs/screenshots/harbor-personal-current.jpg" alt="Harbor Personal privacy dashboard" width="45%" />
  <img src="docs/screenshots/harbor-work-current.webp" alt="Harbor Work app manager" width="45%" />
</p>

<p align="center"><sub>Current Harbor Personal and Work interfaces.</sub></p>

## How it works

Android work profiles are separate spaces managed by the operating system. Apps inside the Work profile have their own app data and appear with Android's work badge.

Harbor uses Android's supported `DevicePolicyManager` APIs. The Harbor copy inside Work becomes the profile owner and performs management actions locally. Harbor respects Android's one-managed-profile-per-parent-user model rather than trying to bypass it.

## Getting started

1. Install Harbor.
2. Open Harbor on the Personal side.
3. Tap **Create Work space**.
4. Follow Android's Work-profile setup screens.
5. Open the work-badged Harbor app to manage apps inside Work.

Harbor is intended for distribution through **F-Droid**. Until the F-Droid build is accepted, alpha builds are available from [GitHub Releases](https://github.com/Stem0794/harbor/releases/latest).

The GitHub release uses the Monstera signing identity for testing. A future F-Droid build will use F-Droid's signing key, so a GitHub-signed installation may not upgrade directly to the F-Droid-signed package.

## Personal to Work file sharing

Harbor supports sending files **from Personal to Work**. Selected files are copied into:

```text
Downloads/Harbor
```

inside Work. The original Personal file is not deleted. Harbor does not provide a Work-to-Personal export flow.

## Privacy

Harbor is designed to work locally:

- No `INTERNET` permission.
- No analytics or telemetry.
- No advertising.
- No account system.
- No Firebase or Google Play services dependency.
- No remote configuration.
- App and user diagnostics stay on the device.

Harbor declares `QUERY_ALL_PACKAGES` because its local Work app catalog needs to enumerate installed applications. This permission does not grant access to another app's private data, Internet access, or arbitrary cross-user access.

See [Privacy](docs/PRIVACY.md) and [Threat model](docs/THREAT_MODEL.md).

## Advanced tools and additional workspaces

Shizuku is optional and must be installed and started separately by the user. Advanced tools can provide local diagnostics, package cloning, Android-user discovery, secondary-user creation where supported, Harbor installation for those users, and user switching.

Additional workspaces are experimental and depend on Android/OEM support. Harbor intentionally does not implement destructive full-user deletion.

## Compatibility

Harbor targets Android 10 through Android 16 (API 29-36). Work-profile provisioning and device-policy behavior can differ by manufacturer.

### Physical devices tested

- Samsung Galaxy S24

The OnePlus 13 remains planned secondary coverage and has not been tested yet.

See [Compatibility](docs/COMPATIBILITY.md) for emulator results and device-specific limitations.

## Current release

Current release line: **0.2.0-alpha05**

The core path includes managed-profile provisioning, local profile-owner detection, the searchable Work app catalog, app launch/details/uninstall, freeze/unfreeze, batch operations, Work-app shortcuts, Personal-to-Work file sharing, and local lifecycle/recovery handling.

Advanced Shizuku operations, secondary-user workspaces, some launcher behavior, and some OEM-specific flows remain experimental.

## For developers

### Requirements

- JDK 17
- Android SDK Platform 36
- Android Build Tools 36.0.0

### Build and verify

```shell
./gradlew testDebugUnitTest lintDebug assembleRelease
./gradlew generateSbom
```

Debug builds use `com.monstera.harbor.debug`. Production identity is `com.monstera.harbor`, with DPC receiver `com.monstera.harbor.admin.HarborDeviceAdminReceiver`.

### Project structure

```text
app/                    Compose UI, DPC receiver, manifests
core/policy/            Public DevicePolicyManager facade
core/data/              Local app catalog and preferences
core/topology/          Users, profiles, ownership, capabilities
privileged/shizuku/     Isolated Shizuku backend
feature/advanced/       Shizuku and multiple-user UI
build-logic/            Shared Gradle conventions
docs/                   Architecture, privacy, threat model, release guidance
packaging/fdroid/       F-Droid metadata and build recipe
```

Before contributing, read [Architecture](docs/ARCHITECTURE.md), [Threat model](docs/THREAT_MODEL.md), [Compatibility](docs/COMPATIBILITY.md), [Dependencies](docs/DEPENDENCIES.md), and [Releasing](docs/RELEASING.md).

## License

Apache License 2.0.

Harbor is behaviorally inspired by Island, but it is a clean implementation with its own application identity and public-API-first architecture.
