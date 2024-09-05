import com.pantelisstampoulis.utils.namespaceWithProjectPackage

plugins {
    id(libs.plugins.custom.library.core.get().pluginId)
}

android {
    namespace = namespaceWithProjectPackage(suffix = "test.doubles.model")
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.utils.random)
}
