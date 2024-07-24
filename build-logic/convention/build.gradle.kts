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

        register("libraryKoin") {
            id = "com.pantelisstampoulis.library.koin"
            implementationClass = "com.pantelisstampoulis.plugin.KoinPlugin"
        }

        register("libraryKotlinSerialization") {
            id = "com.pantelisstampoulis.library.kotlin.serialization"
            implementationClass = "com.pantelisstampoulis.plugin.KotlinSerializationPlugin"
        }

        register("lint") {
            id = "com.pantelisstampoulis.lint"
            implementationClass = "com.pantelisstampoulis.plugin.LintPlugin"
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
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

