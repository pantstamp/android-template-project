package com.pantelisstampoulis.plugin

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.Lint
import com.pantelisstampoulis.utils.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class LintPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            when {
                pluginManager.hasPlugin(
                    libs.findPlugin("android.application").get().get().pluginId
                ) ->
                    configure<ApplicationExtension> {
                        lint { configure() }
                    }

                pluginManager.hasPlugin(
                    libs.findPlugin("android.library").get().get().pluginId
                ) ->
                    configure<LibraryExtension> {
                        lint { configure() }
                    }

                else -> {
                    pluginManager.apply(
                        libs.findPlugin("android.lint").get().get().pluginId
                    )
                    configure<Lint> {
                        configure()
                    }
                }
            }
        }
    }
}

private fun Lint.configure(
    excludeList: List<String> = listOf("ObsoleteLintCustomCheck", "GradleDependency"),
) {
    abortOnError = true
    textReport = false
    xmlReport = true
    htmlReport = true
    ignoreTestSources = true
    checkDependencies = false
    warningsAsErrors = true
    disable.addAll(excludeList)
}
