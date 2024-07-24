import com.pantelisstampoulis.utils.namespaceWithProjectPackage
import java.util.Properties

plugins {
    id(libs.plugins.custom.library.core.get().pluginId)
    id(libs.plugins.custom.koin.get().pluginId)
    id(libs.plugins.custom.kotlin.serialization.get().pluginId)
}

android {
    namespace = namespaceWithProjectPackage(suffix = "network.retrofit")

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        // Load local.properties
        val localProperties = Properties()
        val localPropertiesFile = file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }

        // Retrieve the API key from local.properties or use an empty string if not found
        val apiKey = localProperties.getProperty("TMDB_API_KEY", "")

        buildConfigField("String", "TMDB_API_KEY", "\"$apiKey\"")
    }
}

dependencies {
    implementation(projects.core.network.api)

    implementation(libs.retrofit.core)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit.kotlin.serialization)
}
