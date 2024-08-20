package com.pantelisstampoulis.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

@Suppress("unused")
class LibraryFeaturePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {

            with(pluginManager) {
                apply(LibraryCorePlugin::class.java)
                apply(ComposePlugin::class.java)
                apply(KotlinSerializationPlugin::class.java)
            }

            dependencies {
                // architecture layers
                "implementation"(project(":core:domain"))
                "implementation"(project(":core:presentation:mvi"))
                "implementation"(project(":core:presentation:theme"))
                "implementation"(project(":core:dispatcher:api"))
                "implementation"(project(":architecture:mapper"))
                "implementation"(project(":core:navigation:api"))
            }
        }
    }
}
