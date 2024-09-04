import com.pantelisstampoulis.utils.namespaceWithProjectPackage

plugins {
    id(libs.plugins.custom.library.core.get().pluginId)
}

android {
    namespace = namespaceWithProjectPackage(suffix = "test.doubles.network")
}

dependencies {
    implementation(projects.core.network.api)
    implementation(projects.utils.random)
}
