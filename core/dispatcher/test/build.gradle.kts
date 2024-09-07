import com.pantelisstampoulis.utils.namespaceWithBasePackage

plugins {
    id(libs.plugins.custom.library.core.get().pluginId)
    id(libs.plugins.custom.koin.get().pluginId)
}

android {
    namespace = namespaceWithBasePackage(suffix = "dispatcher.test")
}

dependencies {
    // projects
    implementation(projects.core.dispatcher.api)
    // libraries
    implementation(libs.kotlinx.coroutines.test)
}
