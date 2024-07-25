import com.pantelisstampoulis.utils.namespaceWithBasePackage

plugins {
    id(libs.plugins.custom.library.feature.get().pluginId)
}

android {
    namespace = namespaceWithBasePackage(suffix = "feature.mvi")
}

dependencies {
    // projects
    api(projects.presentation.viewmodel)
    // libraries
    implementation(libs.androidx.annotation)
}
