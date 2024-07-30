import com.pantelisstampoulis.configuration.FlavorProperty
import com.pantelisstampoulis.utils.getFlavoredModule
import com.pantelisstampoulis.utils.namespaceWithProjectPackage

plugins {
    id(libs.plugins.custom.library.core.get().pluginId)
    id(libs.plugins.custom.koin.get().pluginId)
}

android {
    namespace = namespaceWithProjectPackage(suffix = "bridge.di")
}

dependencies {
    // clean architecture layers
    implementation(projects.core.domain)
    implementation(projects.core.data)

    // utilities
    implementation(projects.core.dispatcher.impl)
    implementation(projects.core.logging.api)
    implementation(getFlavoredModule(property = FlavorProperty.Logging))
}
