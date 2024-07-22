package com.pantelisstampoulis.configuration

enum class ProjectProperty(
    val value: String,
) {
    // android configuration
    AndroidTargetSdk(value = "configuration.android.targetSdk"),
    AndroidCompileSdk(value = "configuration.android.compileSdk"),
    AndroidMinSdk(value = "configuration.android.minSdk"),

    // compose
    ComposeMetrics(value = "configuration.compose.metrics"),

    // package
    PackageBase(value = "configuration.package.base"),
    PackageProject(value = "configuration.package.project"),

    // network
    NetworkUrl(value = "configuration.network.url"),
    NetworkUrlPins(value = "configuration.network.url.pins"),
}

