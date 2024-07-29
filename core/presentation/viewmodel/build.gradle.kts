import com.pantelisstampoulis.utils.namespaceWithBasePackage

plugins {
    id(libs.plugins.custom.library.core.get().pluginId)
}

android {
    namespace = namespaceWithBasePackage(suffix = "presentation.viewmodel")
}

dependencies {
    // libraries
    api(libs.kotlinx.coroutines.core)
    api(libs.androidx.lifecycle.viewmodel)
}
