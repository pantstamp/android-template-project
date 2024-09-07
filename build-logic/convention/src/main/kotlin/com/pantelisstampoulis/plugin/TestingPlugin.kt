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
                "testImplementation"(project(":test:doubles:database"))
                "testImplementation"(project(":test:doubles:network"))
                "testImplementation"(project(":test:doubles:model"))

                "androidTestImplementation"(project(":test:doubles:database"))
                "androidTestImplementation"(project(":test:doubles:network"))
                "androidTestImplementation"(project(":test:doubles:model"))

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
