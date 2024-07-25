import com.pantelisstampoulis.utils.namespaceWithProjectPackage

plugins {
    id(libs.plugins.custom.library.core.get().pluginId)
}

android {
    namespace = namespaceWithProjectPackage(suffix = "preferences")
}

dependencies {
    // libraries
    api(libs.kotlinx.coroutines.core)
}
