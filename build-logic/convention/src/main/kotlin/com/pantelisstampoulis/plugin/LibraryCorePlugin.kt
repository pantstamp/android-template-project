package com.pantelisstampoulis.plugin

import com.android.build.gradle.LibraryExtension
import com.pantelisstampoulis.configuration.ProjectProperty
import com.pantelisstampoulis.utils.configureAndroid
import com.pantelisstampoulis.configuration.configureFlavors
import com.pantelisstampoulis.utils.javaVersion
import com.pantelisstampoulis.utils.libs
import com.pantelisstampoulis.utils.requiredIntProperty
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
class LibraryCorePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {

            with(pluginManager) {
                apply(libs.findPlugin("android.library").get().get().pluginId)
                apply(libs.findPlugin("jetbrains.kotlin.android").get().get().pluginId)
                apply(KotlinBasePlugin::class.java)
                apply(LintPlugin::class.java)
                apply(SpotlessPlugin::class.java)
            }

            val compileSdk = requiredIntProperty(property = ProjectProperty.AndroidCompileSdk)
            val minSdk = requiredIntProperty(property = ProjectProperty.AndroidMinSdk)
            val targetSdk = requiredIntProperty(property = ProjectProperty.AndroidTargetSdk)
            val javaVersionFromLibs = javaVersion

            extensions.configure(LibraryExtension::class.java) {
                configureAndroid(
                    compileSdk = compileSdk,
                    minSdk = minSdk,
                    javaVersion = javaVersionFromLibs,
                )
                defaultConfig.targetSdk = targetSdk
                defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                testOptions.animationsDisabled = true
                configureFlavors(this)
            }
        }
    }
}
