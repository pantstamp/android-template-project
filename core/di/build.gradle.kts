import com.pantelisstampoulis.configuration.FlavorProperty
import com.pantelisstampoulis.utils.getFlavoredModule
import com.pantelisstampoulis.utils.namespaceWithProjectPackage

plugins {
    id(libs.plugins.custom.library.core.get().pluginId)
    id(libs.plugins.custom.koin.get().pluginId)
}

android {
    namespace = namespaceWithProjectPackage(suffix = "di")
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(getFlavoredModule(property = FlavorProperty.Network))
    implementation(getFlavoredModule(property = FlavorProperty.Database))
    implementation(projects.core.dispatcher.impl)

    implementation(projects.core.logging.api)
    implementation(getFlavoredModule(property = FlavorProperty.Logging))
    implementation(projects.core.presentation.mvi)
    implementation(projects.core.presentation.viewmodel)

    // features
    implementation(projects.feature.movieCatalog)
}
