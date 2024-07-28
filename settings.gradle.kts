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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AndroidTemplateProject"
include(":app")
include(":core:di")
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
    ":utils:koin"
)

include(
    ":architecture:mapper"
)

include(
    ":presentation:mvi",
    ":presentation:viewmodel"
)

include(":core:data")

include(
    ":core:preferences:api",
    ":core:preferences:datastore",
    ":core:preferences:noop",
)

include(
    ":feature:movie-catalog",
)



