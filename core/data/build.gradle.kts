import com.pantelisstampoulis.utils.namespaceWithProjectPackage

plugins {
    id(libs.plugins.custom.library.core.get().pluginId)
    id(libs.plugins.custom.koin.get().pluginId)
    id(libs.plugins.custom.testing.get().pluginId)
}

android {
    namespace = namespaceWithProjectPackage(suffix = "data")
}

dependencies {
    // architecture layers
    implementation(projects.core.model)
    implementation(projects.core.domain)

    // projects
    implementation(projects.core.database.api)
    implementation(projects.core.network.api)
    implementation(projects.core.network.retrofit)
    implementation(projects.core.database.room)

    // utilities
    implementation(projects.core.logging.api)
    implementation(projects.architecture.mapper)

    // libraries
    implementation(projects.utils.koin)
    implementation(libs.kotlinx.datetime)
    implementation(libs.androidx.annotation)
}
