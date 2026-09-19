package com.pantelisstampoulis.utils

import com.diffplug.gradle.spotless.SpotlessExtension

fun SpotlessExtension.configure(
    ktlintVersion: String,
    editorConfigOverride: Map<String, String> = mapOf(
        "ktlint_standard_property-naming" to "disabled",
        "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
        // ktlint's "ktlint_official" style defaults max_line_length to 140, which lets
        // the function-expression-body rule inline expression bodies onto the signature
        // line and produce declarations up to 140 characters wide. 120 keeps them wrapped.
        "max_line_length" to "120",
        // Keep the project's existing convention of multi-line constructors with trailing
        // commas; ktlint 1.8 would otherwise collapse two-parameter constructors onto a
        // single line wherever they happen to fit. Deliberately not applied to functions,
        // where it would split short signatures such as (call, response) across lines.
        "ktlint_class_signature_rule_force_multiline_when_parameter_count_greater_or_equal_than" to "2",
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
