import com.pantelisstampoulis.utils.namespaceWithProjectPackage

plugins {
    id(libs.plugins.custom.library.core.get().pluginId)
}

android {
    namespace = namespaceWithProjectPackage(suffix = "test.doubles.database")
}

dependencies {
    implementation(projects.core.database.api)
    implementation(projects.utils.random)
}
