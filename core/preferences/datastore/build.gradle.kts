import com.pantelisstampoulis.utils.namespaceWithProjectPackage

plugins {
    id(libs.plugins.custom.library.core.get().pluginId)
    id(libs.plugins.custom.koin.get().pluginId)
}

android {
    namespace = namespaceWithProjectPackage(suffix = "preferences.datastore")

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField(
            type = "String",
            name = "DatastoreFileName",
            value = "datastore/settings.preferences_pb"
        )
    }
}

dependencies {
    // projects
    implementation(projects.core.dispatcher.api)
    implementation(projects.core.preferences.api)
    // libraries
    implementation(libs.androidx.datastore.preferences.core)
}

