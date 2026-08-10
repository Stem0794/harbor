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

`gradle/verification-metadata.xml` pins downloaded artifacts by SHA-256, and per-project lockfiles pin resolved versions. Run `./gradlew generateSbom` to generate `build/reports/sbom/cyclonedx-release.json`, a deterministic CycloneDX component inventory for the release runtime. Artifact hashes remain in Gradle's verification metadata. Unknown licenses in that generated report must be resolved manually before a release; this document is not a substitute for reviewing upstream notices and transitive dependencies.
