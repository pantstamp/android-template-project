import com.pantelisstampoulis.utils.namespaceWithProjectPackage

plugins {
    id(libs.plugins.library.core.get().pluginId)
    id(libs.plugins.library.koin.get().pluginId)
}

android {
    namespace = namespaceWithProjectPackage(suffix = "di")
}
