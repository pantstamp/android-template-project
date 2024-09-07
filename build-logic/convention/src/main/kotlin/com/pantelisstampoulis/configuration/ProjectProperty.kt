package com.pantelisstampoulis.configuration

enum class ProjectProperty(
    val value: String,
) {
    // android configuration
    AndroidTargetSdk(value = "configuration.android.targetSdk"),
    AndroidCompileSdk(value = "configuration.android.compileSdk"),
    AndroidMinSdk(value = "configuration.android.minSdk"),

    // package
    PackageBase(value = "configuration.package.base"),
    PackageProject(value = "configuration.package.project"),

}

