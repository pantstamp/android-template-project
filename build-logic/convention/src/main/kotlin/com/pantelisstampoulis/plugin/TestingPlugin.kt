package com.pantelisstampoulis.plugin

import com.pantelisstampoulis.utils.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.project

@Suppress("unused")
class TestingPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {

            with(pluginManager) {
                apply(libs.findPlugin("ksp").get().get().pluginId)
            }

            dependencies {
                "implementation"(project(":test:doubles:database"))
                "implementation"(project(":test:doubles:network"))

                add("testImplementation", kotlin("test"))
                add("testImplementation",libs.findLibrary("androidx.junit").get())
                add("testImplementation",libs.findLibrary("kotlinx.coroutines.test").get())
                add("testImplementation",libs.findLibrary("truth").get())
                add("testImplementation", libs.findLibrary("mockative").get())
                add("testImplementation", libs.findLibrary("turbine").get())

                add("androidTestImplementation", kotlin("test"))
                add("androidTestImplementation",libs.findLibrary("androidx.junit").get())
                add("androidTestImplementation",libs.findLibrary("kotlinx.coroutines.test").get())
                add("androidTestImplementation",libs.findLibrary("truth").get())
                add("androidTestImplementation",libs.findLibrary("androidx.test.runner").get())
                add("androidTestImplementation", libs.findLibrary("mockative").get())
                add("androidTestImplementation", libs.findLibrary("turbine").get())

                add("kspTest", libs.findLibrary("mockative.processor").get())
            }
        }
    }
}
