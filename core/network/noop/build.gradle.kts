import com.sregs.utils.namespaceWithProjectPackage

plugins {
    id(libs.plugins.kmm.library.core.get().pluginId)
    id(libs.plugins.kmm.library.koin.get().pluginId)
    id(libs.plugins.kotlinx.kover.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // projects
            implementation(projects.core.network.api)
        }
        commonTest.dependencies {
            // libraries
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = namespaceWithProjectPackage(suffix = "network.noop")
}
