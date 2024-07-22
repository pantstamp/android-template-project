package com.pantelisstampoulis.utils

import com.pantelisstampoulis.configuration.FlavorProperty
import com.pantelisstampoulis.configuration.NoopFlavor
import com.pantelisstampoulis.configuration.ProjectProperty
import org.gradle.api.Project

fun Project.namespaceWithBasePackage(
    suffix: String,
    basePackage: String = requiredStringProperty(property = ProjectProperty.PackageBase)
) = "$basePackage.$suffix"

fun Project.namespaceWithProjectPackage(
    suffix: String,
    projectPackage: String = requiredStringProperty(property = ProjectProperty.PackageProject)
) = "$projectPackage.$suffix"

fun Project.getUrl(): String =
    requiredStringProperty(property = ProjectProperty.NetworkUrl)

fun Project.getUrlPins(): String =
    requiredStringProperty(property = ProjectProperty.NetworkUrlPins)

fun Project.isNetworkLoggingEnabled(): Boolean =
    getFlavoredModuleString(property = FlavorProperty.Logging, short = true) != NoopFlavor
