pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Harbor"

include(
    ":app",
    ":core:data",
    ":core:policy",
    ":core:topology",
    ":feature:advanced",
    ":privileged:shizuku",
)
