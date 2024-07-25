import com.pantelisstampoulis.utils.namespaceWithProjectPackage

plugins {
    id(libs.plugins.custom.library.core.get().pluginId)
    id(libs.plugins.custom.koin.get().pluginId)
}

android {
    namespace = namespaceWithProjectPackage(suffix = "domain")
}

dependencies {
    api(projects.core.model)
    implementation(projects.core.logging.api)
    implementation(projects.utils.koin)
    implementation(projects.core.dispatcher.api)
}