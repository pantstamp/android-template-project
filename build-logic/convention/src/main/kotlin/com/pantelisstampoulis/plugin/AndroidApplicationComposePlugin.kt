package com.pantelisstampoulis.plugin

import com.android.build.api.dsl.ApplicationExtension
import com.pantelisstampoulis.utils.configureAndroidCompose
import com.pantelisstampoulis.utils.configureCompose
import com.pantelisstampoulis.utils.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

class AndroidApplicationComposePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {

            with(pluginManager) {
                apply(AndroidApplicationCorePlugin::class.java)
                apply(libs.findPlugin("compose-compiler").get().get().pluginId)
            }

            extensions.getByType<ApplicationExtension>().configureAndroidCompose()
            val composeExtension = extensions.getByType<ComposeCompilerGradlePluginExtension>()
            configureCompose(extension = composeExtension)

            dependencies {
                "implementation"(dependencies.platform(libs.findLibrary("compose.bom").get()))
                "implementation"(libs.findBundle("compose.common").get())
                "implementation"(libs.findLibrary("androidx.lifecycle.runtime.compose").get())
                "implementation"(libs.findLibrary("androidx.navigation.compose").get())
                "implementation"(libs.findLibrary("koin.androidx.compose").get())
            }
        }
    }
}
