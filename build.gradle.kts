import org.gradle.api.artifacts.component.ModuleComponentIdentifier

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
}

fun String.jsonEscaped(): String = buildString {
    this@jsonEscaped.forEach { character ->
        when (character) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> append(character)
        }
    }
}

fun licenseFor(group: String): String? = when {
    group.startsWith("androidx.") -> "Apache-2.0"
    group.startsWith("org.jetbrains.") -> "Apache-2.0"
    group == "org.jetbrains" -> "Apache-2.0"
    group == "dev.rikka.shizuku" -> "Apache-2.0"
    group == "org.jspecify" -> "Apache-2.0"
    group == "com.google.guava" -> "Apache-2.0"
    group.startsWith("com.squareup.") -> "Apache-2.0"
    else -> null
}

project(":app") {
    tasks.register("generateReleaseSbom") {
        group = "verification"
        description = "Generate a deterministic CycloneDX JSON inventory for the release runtime."

        doLast {
            val modules = configurations.getByName("releaseRuntimeClasspath")
            .incoming.resolutionResult.allComponents
            .mapNotNull { it.id as? ModuleComponentIdentifier }
            .distinctBy { "${it.group}:${it.module}:${it.version}" }
            .sortedWith(
                compareBy(
                    { it.group },
                    { it.module },
                    { it.version },
                ),
            )
            val components = modules.joinToString(",\n") { module ->
                val license = licenseFor(module.group)?.let { spdx ->
                    ",\n      \"licenses\": [{\"license\": {\"id\": \"$spdx\"}}]"
                }.orEmpty()
                """    {
      "type": "library",
      "group": "${module.group.jsonEscaped()}",
      "name": "${module.module.jsonEscaped()}",
      "version": "${module.version.jsonEscaped()}",
      "purl": "pkg:maven/${module.group.jsonEscaped()}/${module.module.jsonEscaped()}@${module.version.jsonEscaped()}"$license
    }"""
            }
            val report = rootProject.layout.buildDirectory
                .file("reports/sbom/cyclonedx-release.json").get().asFile
            report.parentFile.mkdirs()
            report.writeText(
                """{
  "bomFormat": "CycloneDX",
  "specVersion": "1.5",
  "version": 1,
  "metadata": {
    "component": {
      "type": "application",
      "group": "com.monstera",
      "name": "harbor",
      "version": "${providers.gradleProperty("harbor.versionName").get().jsonEscaped()}"
    }
  },
  "components": [
$components
  ]
}
""",
            )
            logger.lifecycle("Wrote ${report.relativeTo(rootDir)}")
        }
    }
}

tasks.register("generateSbom") {
    group = "verification"
    description = "Generate the release runtime SBOM."
    dependsOn(":app:generateReleaseSbom")
}

subprojects {
    dependencyLocking {
        lockAllConfigurations()
    }
}
