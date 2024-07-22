package com.pantelisstampoulis.utils

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal val Project.javaVersion: JavaVersion
    get() {
        val javaVersionInt = libs.findVersion("java.version").get().toString().toInt()
        return JavaVersion.values()[javaVersionInt - 1]
    }

internal val Project.jvmTarget: JvmTarget
    get() {
        val javaVersion = libs.findVersion("java.version").get().toString()
        return JvmTarget.values().first { target -> target.toString().contains(javaVersion) }
    }
