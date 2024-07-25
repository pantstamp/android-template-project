import com.pantelisstampoulis.utils.namespaceWithProjectPackage

plugins {
    id(libs.plugins.custom.library.core.get().pluginId)
    id(libs.plugins.custom.koin.get().pluginId)
}

android {
    namespace = namespaceWithProjectPackage(suffix = "data")
}

dependencies {
    // clean architecture layers
    implementation(projects.core.model)
    implementation(projects.core.domain)
    implementation(projects.core.database.api)
    implementation(projects.core.network.api)

    // utilities
    implementation(projects.core.logging.api)
    implementation(projects.core.preferences.api)
    implementation(projects.architecture.mapper)

    // 3rd party libraries
    implementation(projects.utils.koin)
    implementation(libs.kotlinx.datetime)
    implementation(libs.androidx.annotation)
}
