import com.pantelisstampoulis.utils.namespaceWithBasePackage

plugins {
    id(libs.plugins.custom.library.core.get().pluginId)
}

android {
    namespace = namespaceWithBasePackage(suffix = "architecture.mapper")
}

dependencies {
    // projects
    //api(projects.core.presentation.viewmodel)
    // libraries
    implementation(libs.androidx.annotation)
}
