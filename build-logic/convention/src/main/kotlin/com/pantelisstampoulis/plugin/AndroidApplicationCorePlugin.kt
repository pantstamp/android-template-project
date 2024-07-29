package com.pantelisstampoulis.plugin

import com.android.build.api.dsl.ApplicationExtension
import com.pantelisstampoulis.configuration.ProjectProperty
import com.pantelisstampoulis.utils.configureAndroid
import com.pantelisstampoulis.utils.javaVersion
import com.pantelisstampoulis.utils.libs
import com.pantelisstampoulis.utils.requiredIntProperty
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

class AndroidApplicationCorePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.findPlugin("android.application").get().get().pluginId)
                apply(libs.findPlugin("kotlin.android").get().get().pluginId)
                apply(KotlinBasePlugin::class.java)
                apply(LintPlugin::class.java)
                apply(KoinPlugin::class.java)
            }

            val compileSdk = requiredIntProperty(property = ProjectProperty.AndroidCompileSdk)
            val minSdk = requiredIntProperty(property = ProjectProperty.AndroidMinSdk)
            val targetSdk = requiredIntProperty(property = ProjectProperty.AndroidTargetSdk)
            val javaVersionFromLibs = javaVersion

            extensions.configure(ApplicationExtension::class.java) {
                configureAndroid(
                    compileSdk = compileSdk,
                    minSdk = minSdk,
                    javaVersion = javaVersionFromLibs,
                )
                defaultConfig.targetSdk = targetSdk
            }

            dependencies {
                // architecture layers
                "implementation"(project(":core:di"))
                "implementation"(project(":core:domain"))
                "implementation"(project(":core:presentation:mvi"))
                "implementation"(project(":core:dispatcher:api"))
                "implementation"(project(":architecture:mapper"))

                // libraries
                "implementation"(libs.findLibrary("material").get())

            }
        }
    }
}
