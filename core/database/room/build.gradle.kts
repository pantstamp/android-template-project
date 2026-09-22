import com.pantelisstampoulis.utils.namespaceWithProjectPackage

plugins {
    id(libs.plugins.custom.library.core.get().pluginId)
    id(libs.plugins.custom.koin.get().pluginId)
    id(libs.plugins.custom.room.get().pluginId)
    id(libs.plugins.custom.testing.get().pluginId)
}

android {
    namespace = namespaceWithProjectPackage(suffix = "database.room")
}

dependencies {
    implementation(projects.core.database.api)

    implementation(libs.retrofit.core)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit.kotlin.serialization)

    androidTestImplementation(projects.utils.random)
}

