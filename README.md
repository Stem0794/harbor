<p align="center">
  <img src="docs/branding/harbor-logo.svg" alt="Harbor lighthouse logo" width="128" />
</p>

<h1 align="center">Harbor</h1>

<p align="center"><strong>A private space for Android apps, built on Android's work-profile system.</strong></p>

<p align="center">
  <a href="https://f-droid.org/packages/com.monstera.harbor/"><img alt="F-Droid" src="https://img.shields.io/badge/F--Droid-Get%20Harbor-1976D2?logo=fdroid" /></a>
  <a href="https://github.com/Stem0794/harbor/releases/latest"><img alt="GitHub release" src="https://img.shields.io/github/v/release/Stem0794/harbor?include_prereleases&label=GitHub" /></a>
  <img alt="Android 10+" src="https://img.shields.io/badge/Android-10%2B-3DDC84?logo=android&logoColor=white" />
  <a href="LICENSE"><img alt="Apache License 2.0" src="https://img.shields.io/badge/License-Apache--2.0-blue" /></a>
</p>

<p align="center">
  <a href="https://f-droid.org/packages/com.monstera.harbor/"><strong>Install from F-Droid</strong></a>
  ·
  <a href="https://github.com/Stem0794/harbor/releases/latest">GitHub Releases</a>
</p>

Harbor is a free and open-source Android work-profile manager. It creates an isolated Work space where apps keep separate data from the Personal side of the phone.

Harbor does **not** require Google Play services, root, an account, analytics, advertising, or the `INTERNET` permission for its core features.

> **Harbor is alpha software.** Use it for testing and non-critical data while device compatibility is still being expanded.

## Screenshots

<p align="center">
  <img src="docs/screenshots/harbor-personal-samsung-api36.png" alt="Harbor Personal privacy dashboard" width="45%" />
  <img src="docs/screenshots/harbor-work-current.webp" alt="Harbor Work app manager" width="45%" />
</p>

<p align="center"><sub>Harbor Personal and Work interfaces.</sub></p>

## Why Harbor

Harbor is designed to keep its normal Work-profile path local and based on Android's supported APIs.

| | Harbor |
|---|---|
| Internet permission | No |
| Analytics or telemetry | No |
| Advertising | No |
| Account required | No |
| Google Play services required | No |
| Root required | No |
| Shizuku required for core features | No |
| Core management API | Android `DevicePolicyManager` |

## Core features

- Create a standard Android work profile.
- Show and search apps installed inside Work.
- Launch apps and open Android app details.
- Freeze and unfreeze eligible apps.
- Perform batch app-management actions.
- Use Android's confirmation flow for uninstall.
- Create launcher shortcuts for Work apps.
- Send selected files from Personal to Work.
- Prepare APK installation while Android keeps its normal source-consent prompts.

Shizuku is **not required** for these normal Work-profile features.

## Optional advanced tools

Shizuku can be enabled separately for advanced functionality, including:

- Local diagnostics.
- Package cloning.
- Android-user discovery.
- Secondary-user creation where supported.
- Harbor installation for those users.
- User switching.
- Experimental additional workspaces.

Additional workspaces depend on Android and OEM support. Harbor intentionally does not provide destructive full-user deletion.

## How Harbor works

Android work profiles are separate spaces managed by the operating system. Apps inside Work have their own app data and appear with Android's work badge.

```text
Your phone
├── Personal
│   ├── Harbor
│   └── Personal apps
│
└── Work
    ├── Harbor (profile owner)
    └── Isolated Work apps
```

Harbor uses Android's supported `DevicePolicyManager` APIs. The Harbor copy inside Work becomes the profile owner and performs management actions locally. Harbor respects Android's one-managed-profile-per-parent-user model rather than trying to bypass it.

## Install

### F-Droid

[**Install Harbor from F-Droid**](https://f-droid.org/packages/com.monstera.harbor/)

F-Droid is the recommended installation source.

### GitHub Releases

Alpha APKs are also available from [GitHub Releases](https://github.com/Stem0794/harbor/releases/latest).

> **Signing note:** the F-Droid build is signed by F-Droid, while GitHub releases use the Monstera signing identity. Because the signing keys differ, a GitHub-signed installation may not upgrade directly to the F-Droid-signed package.

## Create your Work space

1. Install and open Harbor on the Personal side.
2. Tap **Create Work space**.
3. Follow Android's Work-profile setup screens.
4. Open the work-badged Harbor app.
5. Manage apps inside Work from there.

## Personal to Work file sharing

Harbor supports sending files **from Personal to Work**. Selected files are copied into:

```text
Downloads/Harbor
```

inside Work. The original Personal file is not deleted. Harbor does not provide a Work-to-Personal export flow.

## Current limitations

Harbor is still alpha software, and some behavior remains device-dependent.

- Work-profile provisioning and device-policy behavior can differ by manufacturer.
- Additional Shizuku workspaces are experimental.
- Some launcher behavior remains device-dependent.
- Harbor supports Personal-to-Work file transfer, not Work-to-Personal export.
- Android generally permits one managed profile per parent user.

See [Compatibility](docs/COMPATIBILITY.md) for tested configurations and device-specific limitations.

## Compatibility

Harbor targets Android 10 through Android 16 (API 29-36).

### Physical devices tested

- Samsung Galaxy S24

The OnePlus 13 remains planned secondary coverage and has not been tested yet.

See [Compatibility](docs/COMPATIBILITY.md) for emulator results and device-specific limitations.

## Privacy and security

Harbor is designed to work locally:

- No `INTERNET` permission.
- No analytics or telemetry.
- No advertising.
- No account system.
- No Firebase or Google Play services dependency.
- No remote configuration.
- App and user diagnostics stay on the device.

Harbor declares `QUERY_ALL_PACKAGES` because its local Work app catalog needs to enumerate installed applications. This permission does not grant access to another app's private data, Internet access, or arbitrary cross-user access.

For the security model and design boundaries, see [Privacy](docs/PRIVACY.md), [Threat model](docs/THREAT_MODEL.md), and [Architecture](docs/ARCHITECTURE.md).

## FAQ

### Does Harbor require root?

No.

### Does Harbor require Shizuku?

No. Shizuku is optional and only used for advanced functionality.

### Does Harbor require Google Play services?

No.

### Does Harbor access the Internet?

The core app does not declare the `INTERNET` permission.

### Can Harbor send files from Work back to Personal?

No. Harbor currently provides Personal-to-Work file transfer only.

### Can I switch directly from a GitHub APK to the F-Droid build?

A normal in-place upgrade may not work because the GitHub and F-Droid builds use different signing keys.

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
