package com.pantelisstampoulis.plugin

import com.android.build.gradle.LibraryExtension
import com.pantelisstampoulis.utils.configureAndroidCompose
import com.pantelisstampoulis.utils.configureCompose
import com.pantelisstampoulis.utils.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.project
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

@Suppress("unused")
class LibraryFeaturePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {

            with(pluginManager) {
                apply(LibraryCorePlugin::class.java)
                apply(ComposePlugin::class.java)
            }

            dependencies {
                // architecture layers
                "implementation"(project(":core:presentation:mvi"))
                "implementation"(project(":core:dispatcher:api"))
                "implementation"(project(":core:domain"))
                "implementation"(project(":architecture:mapper"))
            }
        }
    }
}
