package com.pantelisstampoulis.plugin

import com.diffplug.gradle.spotless.SpotlessExtension
import com.pantelisstampoulis.utils.configure
import com.pantelisstampoulis.utils.libs
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
class SpotlessPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.findPlugin("spotless").get().get().pluginId)

            extensions.configure(SpotlessExtension::class.java) {
                configure(
                    ktlintVersion = libs.findVersion("ktlint").get().toString(),
                )
            }
        }
    }
}
