package com.pantelisstampoulis.utils

import com.pantelisstampoulis.configuration.ProjectProperty
import org.gradle.api.GradleException
import org.gradle.api.Project

internal fun Project.requiredIntProperty(property: ProjectProperty): Int =
    requiredProjectProperty(property) { it.toString().toInt() }

private fun <T> Project.requiredProjectProperty(
    property: ProjectProperty, mapper: (Any) -> T
): T {
    val anyValue: Any = property(property.value)
        ?: throw GradleException("Required project property ${property.value} not defined!")

    return try {
        mapper(anyValue)
    } catch (e: Exception) {
        throw GradleException("Can't map project property ${property.value} to required type", e)
    }
}

internal fun Project.requiredStringProperty(property: ProjectProperty): String =
    requiredProjectProperty(property) { it.toString() }

internal fun Project.requiredBooleanProperty(property: ProjectProperty): Boolean =
    requiredProjectProperty(property) { it.toString().toBoolean() }
