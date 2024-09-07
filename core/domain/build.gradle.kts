import com.pantelisstampoulis.utils.namespaceWithProjectPackage

plugins {
    id(libs.plugins.custom.library.core.get().pluginId)
    id(libs.plugins.custom.koin.get().pluginId)
}

android {
    namespace = namespaceWithProjectPackage(suffix = "domain")
}

dependencies {
    // architecture layers
    api(projects.core.model)

    // utilities
    implementation(projects.utils.koin)
    implementation(projects.core.logging.api)
    implementation(projects.core.dispatcher.api)
}