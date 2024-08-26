package com.pantelisstampoulis.plugin

import com.android.build.gradle.LibraryExtension
import com.pantelisstampoulis.utils.configureAndroidCompose
import com.pantelisstampoulis.utils.configureCompose
import com.pantelisstampoulis.utils.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

@Suppress("unused")
class TestingPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {

            dependencies {

                add("testImplementation", kotlin("test"))
                add("testImplementation",libs.findLibrary("androidx.junit").get())
                add("testImplementation",libs.findLibrary("kotlinx.coroutines.test").get())
                add("testImplementation",libs.findLibrary("truth").get())


                add("androidTestImplementation", kotlin("test"))
                add("androidTestImplementation",libs.findLibrary("androidx.junit").get())
                add("androidTestImplementation",libs.findLibrary("kotlinx.coroutines.test").get())
                add("androidTestImplementation",libs.findLibrary("truth").get())
                add("androidTestImplementation",libs.findLibrary("androidx.test.runner").get())
            }
        }
    }
}
