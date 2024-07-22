package com.pantelisstampoulis.utils

import org.gradle.api.Project
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

internal fun Project.configureCompose(
    extension: ComposeCompilerGradlePluginExtension,
) {
    extension.apply {
        targetKotlinPlatforms.set(
            KotlinPlatformType.values()
                .filter { it == KotlinPlatformType.androidJvm }
                .asIterable()
        )

        val buildDir = layout.buildDirectory.get().asFile
        val relativePath = projectDir.relativeTo(rootDir)
        val metricsFolder = buildDir.resolve("compose-metrics").resolve(relativePath)
        val reportsFolder = buildDir.resolve("compose-reports").resolve(relativePath)

        // use -Pandroidx.enableComposeCompilerMetrics=true to enable compose compiler metrics
        metricsDestination.set(metricsFolder)

        // use -Pandroidx.enableComposeCompilerReports=true to enable compose compiler reports
        reportsDestination.set(reportsFolder)

        enableIntrinsicRemember.set(true)
    }
}