package com.pantelisstampoulis.utils

import com.pantelisstampoulis.configuration.FlavorProperty
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

fun Project.getFlavoredModule(
    property: FlavorProperty,
): ProjectDependency {
    val flavoredModule = getFlavoredModuleString(
        property = property
    )

    return project.dependencies.project(
        mapOf("path" to flavoredModule)
    ) as ProjectDependency
}

internal fun Project.getFlavoredModuleString(
    property: FlavorProperty,
    short: Boolean = false,
): String {
    val anyValue: Any = property(property.value)
        ?: throw GradleException("Required flavor property ${property.value} not defined!")

    try {
        return if (short) {
            anyValue.toString()
        } else {
            property.getFlavoredModule(flavor = anyValue.toString())
        }
    } catch (e: Exception) {
        throw GradleException("Can't map flavor property ${property.value} to required type", e)
    }
}
