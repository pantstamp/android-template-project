package com.pantelisstampoulis.plugin

import com.pantelisstampoulis.utils.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

@Suppress("unused")
class KoinPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {

            dependencies {
                "implementation"(dependencies.platform(libs.findLibrary("koin.bom").get()))
                add("implementation", libs.findBundle("koin-common").get())
            }
        }
    }
}
