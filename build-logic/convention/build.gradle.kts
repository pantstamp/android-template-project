plugins {
    `kotlin-dsl`
}

group = "com.pantelisstampoulis.buildlogic"

gradlePlugin {
    plugins {
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
            implementationClass = "com.pantelisstampoulis.plugin.LibraryKoinPlugin"
        }
        register("librarySerialization") {
            id = "com.pantelisstampoulis.library.serialization"
            implementationClass = "com.pantelisstampoulis.plugin.LibrarySerializationPlugin"
        }
        // android
        register("androidApplicationCompose") {
            id = "com.pantelisstampoulis.android.application.compose"
            implementationClass = "com.pantelisstampoulis.plugin.AndroidApplicationComposePlugin"
        }
        register("androidApplicationCore") {
            id = "com.pantelisstampoulis.android.application.core"
            implementationClass = "com.pantelisstampoulis.plugin.AndroidApplicationCorePlugin"
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

