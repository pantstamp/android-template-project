import com.pantelisstampoulis.utils.namespaceWithProjectPackage

plugins {
    id(libs.plugins.custom.library.core.get().pluginId)
    id(libs.plugins.custom.koin.get().pluginId)
}

android {
    namespace = namespaceWithProjectPackage(suffix = "preferences.noop")
}

dependencies {
    // libraries
    implementation(projects.core.preferences.api)
}
