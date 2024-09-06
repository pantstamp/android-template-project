package com.pantelisstampoulis.utils

import com.diffplug.gradle.spotless.SpotlessExtension

fun SpotlessExtension.configure(
    ktlintVersion: String,
    editorConfigOverride: Map<String, String> = mapOf(
        "ktlint_standard_property-naming" to "disabled",
        "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
    )
) {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktlint(ktlintVersion).editorConfigOverride(editorConfigOverride)
    }
    format("kts") {
        target("**/*.kts")
        targetExclude("**/build/**/*.kts")
    }
    format("xml") {
        target("**/*.xml")
        targetExclude("**/build/**/*.xml")
    }
}
