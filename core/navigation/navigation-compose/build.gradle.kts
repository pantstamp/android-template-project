import com.pantelisstampoulis.utils.namespaceWithProjectPackage

plugins {
    id(libs.plugins.custom.library.core.get().pluginId)
    id(libs.plugins.custom.koin.get().pluginId)
    id(libs.plugins.custom.compose.get().pluginId)
    id(libs.plugins.custom.kotlin.serialization.get().pluginId)
}

android {
    namespace = namespaceWithProjectPackage(suffix = "navigation.compose")
}

dependencies {
    implementation(projects.core.navigation.api)
}
