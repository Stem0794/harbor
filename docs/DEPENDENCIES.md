# Dependency and license audit

Harbor resolves dependencies only from Google Maven, Maven Central, and the Gradle Plugin Portal. It does not use JitPack, checked-in binary libraries, Google Play services, Firebase, analytics, crash reporting, or proprietary SDKs.

Production dependency families:

| Family | Purpose | License |
|---|---|---|
| Android Gradle Plugin and AndroidX | Build, lifecycle, DataStore, activity, and platform compatibility | Apache-2.0 |
| Jetpack Compose and Material 3 | User interface | Apache-2.0 |
| Kotlin and kotlinx.coroutines | Language, compiler plugin, and concurrency | Apache-2.0 |
| Shizuku API and provider 13.1.5 | Optional local privileged-service integration | Apache-2.0 |

JUnit 4 is test-only and licensed EPL-1.0. Gradle is build-only and licensed Apache-2.0.

The current direct version pins are Kotlin 2.4.10, AndroidX Core 1.18.0,
Lifecycle 2.10.0, DataStore 1.2.1, Compose BOM 2026.06.01, and
kotlinx.coroutines 1.11.0. AndroidX Lifecycle 2.11.0 and Core 1.19.0 require
compileSdk 37, so they remain deferred until Harbor's supported SDK matrix is
raised deliberately.

`gradle/verification-metadata.xml` pins downloaded artifacts by SHA-256, and per-project lockfiles pin resolved versions. Run `./gradlew generateSbom` to generate `build/reports/sbom/cyclonedx-release.json`, a deterministic CycloneDX component inventory for the release runtime. Artifact hashes remain in Gradle's verification metadata. Unknown licenses in that generated report must be resolved manually before a release; this document is not a substitute for reviewing upstream notices and transitive dependencies.
