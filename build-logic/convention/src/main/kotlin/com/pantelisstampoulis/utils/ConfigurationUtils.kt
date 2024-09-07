package com.pantelisstampoulis.utils

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