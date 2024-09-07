import com.pantelisstampoulis.utils.namespaceWithBasePackage

plugins {
    id(libs.plugins.custom.library.core.get().pluginId)
    id(libs.plugins.custom.koin.get().pluginId)
}

android {
    namespace = namespaceWithBasePackage(suffix = "logging.noop")
}

dependencies {
    implementation(projects.core.logging.api)
}
