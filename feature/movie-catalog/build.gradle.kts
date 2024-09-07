import com.pantelisstampoulis.utils.namespaceWithProjectPackage

plugins {
    id(libs.plugins.custom.library.feature.get().pluginId)
    id(libs.plugins.custom.koin.get().pluginId)
    id(libs.plugins.custom.testing.get().pluginId)
}

android {
    namespace = namespaceWithProjectPackage(suffix = "feature.moviecatalog")
}