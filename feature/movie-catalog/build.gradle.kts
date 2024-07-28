import com.pantelisstampoulis.utils.namespaceWithProjectPackage

plugins {
    id(libs.plugins.custom.library.feature.get().pluginId)
    id(libs.plugins.custom.koin.get().pluginId)
}

android {
    namespace = namespaceWithProjectPackage(suffix = "feature.moviecatalog")
}

dependencies {
    implementation(projects.presentation.mvi)
    implementation(projects.core.dispatcher.api)
    implementation(projects.core.domain)
    implementation(projects.architecture.mapper)
}
