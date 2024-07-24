import com.pantelisstampoulis.utils.namespaceWithProjectPackage

plugins {
    id(libs.plugins.custom.library.core.get().pluginId)
    id(libs.plugins.custom.kotlin.serialization.get().pluginId)
}

android {
    namespace = namespaceWithProjectPackage(suffix = "network")
}

dependencies {
    implementation(libs.kotlinx.datetime)
}