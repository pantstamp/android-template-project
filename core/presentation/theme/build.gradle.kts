import com.pantelisstampoulis.utils.namespaceWithProjectPackage

plugins {
    id(libs.plugins.custom.library.core.get().pluginId)
    id(libs.plugins.custom.compose.get().pluginId)
}

android {
    namespace = namespaceWithProjectPackage(suffix = "theme")
}