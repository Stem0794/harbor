# Contributing

Changes must preserve the separation between public DPC behavior and optional Shizuku behavior.

- Do not add hidden API bypasses, arbitrary shell execution, analytics, proprietary dependencies, or network access.
- Add tests for package/user identifier handling and privileged command construction.
- Document Android-version and OEM assumptions.
- Run `./gradlew testDebugUnitTest lintDebug assembleRelease` before submitting a change.
- Do not rename the production package or DPC receiver after public beta.
