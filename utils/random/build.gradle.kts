import com.pantelisstampoulis.utils.namespaceWithBasePackage

plugins {
    id(libs.plugins.custom.library.core.get().pluginId)
}

android {
    namespace = namespaceWithBasePackage(suffix = "utils.random")
}

dependencies {
    implementation(libs.androidx.annotation)
    api(libs.kotlinx.datetime)

    testImplementation(libs.kotlin.test)
}
