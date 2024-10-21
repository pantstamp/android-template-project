plugins {
    `kotlin-dsl`
}

group = "com.pantelisstampoulis.buildlogic"

gradlePlugin {
    plugins {

        register("androidApplicationCore") {
            id = "com.pantelisstampoulis.application.core"
            implementationClass = "com.pantelisstampoulis.plugin.AndroidApplicationCorePlugin"
        }

        register("androidApplicationCompose") {
            id = "com.pantelisstampoulis.application.compose"
            implementationClass = "com.pantelisstampoulis.plugin.AndroidApplicationComposePlugin"
        }

        register("libraryCore") {
            id = "com.pantelisstampoulis.library.core"
            implementationClass = "com.pantelisstampoulis.plugin.LibraryCorePlugin"
        }

        register("libraryFeature") {
            id = "com.pantelisstampoulis.library.feature"
            implementationClass = "com.pantelisstampoulis.plugin.LibraryFeaturePlugin"
        }

        register("koin") {
            id = "com.pantelisstampoulis.koin"
            implementationClass = "com.pantelisstampoulis.plugin.KoinPlugin"
        }

        register("kotlinSerialization") {
            id = "com.pantelisstampoulis.kotlin.serialization"
            implementationClass = "com.pantelisstampoulis.plugin.KotlinSerializationPlugin"
        }

        register("lint") {
            id = "com.pantelisstampoulis.lint"
            implementationClass = "com.pantelisstampoulis.plugin.LintPlugin"
        }

        register("room") {
            id = "com.pantelisstampoulis.room"
            implementationClass = "com.pantelisstampoulis.plugin.RoomPlugin"
        }

        register("compose") {
            id = "com.pantelisstampoulis.compose"
            implementationClass = "com.pantelisstampoulis.plugin.ComposePlugin"
        }

        register("testing") {
            id = "com.pantelisstampoulis.testing"
            implementationClass = "com.pantelisstampoulis.plugin.TestingPlugin"
        }

        register("spotless") {
            id = "com.pantelisstampoulis.spotless"
            implementationClass = "com.pantelisstampoulis.plugin.SpotlessPlugin"
        }

        register("konsist") {
            id = "com.pantelisstampoulis.konsist"
            implementationClass = "com.pantelisstampoulis.plugin.KonsistPlugin"
        }
    }
}

kotlin {
    jvmToolchain(libs.versions.java.version.get().toInt())
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.compose.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.spotless.gradlePlugin)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

