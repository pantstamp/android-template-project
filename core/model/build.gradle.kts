import com.pantelisstampoulis.utils.namespaceWithProjectPackage

plugins {
    id(libs.plugins.custom.library.core.get().pluginId)
}

android {
    namespace = namespaceWithProjectPackage(suffix = "model")
}

dependencies {

    // projects
    //implementation(projects.core.network.api)

}


