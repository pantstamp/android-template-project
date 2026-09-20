@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
// Resolves the Java toolchain declared in build-logic (java-version in
// libs.versions.toml) by downloading a matching JDK when none is installed
// locally, so the build does not depend on the developer's own JDK setup.
// The version is literal because the version catalog is not yet available
// while settings are being evaluated.
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AndroidTemplateProject"

include(":app")

include(":core:bridge-di")

include(
    ":core:network:api",
    ":core:network:noop",
    ":core:network:retrofit",
)

include(":core:model")

include(
    ":core:database:api",
    ":core:database:noop",
    ":core:database:room"
)
include(":core:domain")

include(
    ":core:logging:api",
    ":core:logging:noop",
)

include(
    ":core:dispatcher:api",
    ":core:dispatcher:impl",
    ":core:dispatcher:test",
)

include(
    ":utils:koin",
    ":utils:random",
)

include(
    ":architecture:mapper"
)

include(
    ":core:presentation:common-ui",
    ":core:presentation:mvi",
    ":core:presentation:viewmodel",
    ":core:presentation:theme"
)

include(":core:data")

include(
    ":core:navigation:api",
    ":core:navigation:navigation-compose",
)

include(
    ":feature:movie-catalog",
)

include(
    ":test:doubles:database",
    ":test:doubles:network",
    ":test:doubles:model",
    ":test:konsist",
)



