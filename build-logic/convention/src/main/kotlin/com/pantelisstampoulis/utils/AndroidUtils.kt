package com.pantelisstampoulis.utils

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion

// AGP 9 moved the `defaultConfig { }` / `compileOptions { }` / `buildFeatures { }`
// block overloads onto the concrete extensions (ApplicationExtension, LibraryExtension),
// because each returns its own type. CommonExtension still exposes them as properties,
// which is what keeps these helpers usable from both plugins.
internal fun CommonExtension.configureAndroid(
    compileSdk: Int,
    minSdk: Int,
    javaVersion: JavaVersion
) {
    this.compileSdk = compileSdk

    defaultConfig.minSdk = minSdk

    compileOptions.apply {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
}

internal fun CommonExtension.configureAndroidCompose() {
    buildFeatures.compose = true
}
