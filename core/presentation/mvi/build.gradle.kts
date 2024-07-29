import com.pantelisstampoulis.utils.namespaceWithBasePackage

plugins {
    id(libs.plugins.custom.library.core.get().pluginId)
    id(libs.plugins.custom.compose.get().pluginId)

}

android {
    namespace = namespaceWithBasePackage(suffix = "presentation.mvi")
}

dependencies {

    // projects
    api(projects.core.presentation.viewmodel)

    // libraries
    implementation(libs.androidx.annotation)

}
