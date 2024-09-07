import com.pantelisstampoulis.utils.namespaceWithBasePackage

plugins {
    id(libs.plugins.custom.library.core.get().pluginId)
    id(libs.plugins.custom.koin.get().pluginId)
}

android {
    namespace = namespaceWithBasePackage(suffix = "dispatcher.impl")
}

dependencies {
    implementation(projects.core.dispatcher.api)
}

