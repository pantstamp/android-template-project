import com.pantelisstampoulis.utils.namespaceWithBasePackage

plugins {
    id(libs.plugins.custom.library.core.get().pluginId)
}

android {
    namespace = namespaceWithBasePackage(suffix = "architecture.mapper")
}

dependencies {
    // libraries
    implementation(libs.androidx.annotation)
}
