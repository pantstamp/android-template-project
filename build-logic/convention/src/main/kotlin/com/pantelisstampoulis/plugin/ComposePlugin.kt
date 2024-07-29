package com.pantelisstampoulis.plugin

import com.android.build.gradle.LibraryExtension
import com.pantelisstampoulis.utils.configureAndroidCompose
import com.pantelisstampoulis.utils.configureCompose
import com.pantelisstampoulis.utils.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

@Suppress("unused")
class ComposePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {

            with(pluginManager) {
                apply(libs.findPlugin("compose-compiler").get().get().pluginId)
            }

            extensions.configure(LibraryExtension::class.java) {
                configureAndroidCompose()
            }

            val composeExtension = extensions.getByType<ComposeCompilerGradlePluginExtension>()
            configureCompose(extension = composeExtension)

            dependencies {
                // libraries
                "implementation"(dependencies.platform(libs.findLibrary("compose.bom").get()))
                "implementation"(libs.findBundle("compose.common").get())
                "implementation"(libs.findLibrary("kotlinx.collections.immutable").get())
            }
        }
    }
}
