package com.pantelisstampoulis.utils

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion

internal fun CommonExtension<*, *, *, *, *, *>.configureAndroid(
    compileSdk: Int,
    minSdk: Int,
    javaVersion: JavaVersion
) {
    this.compileSdk = compileSdk

    defaultConfig {
        this.minSdk = minSdk
    }

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
}

internal fun CommonExtension<*, *, *, *, *, *>.configureAndroidCompose() {
    buildFeatures {
        compose = true
    }
}
