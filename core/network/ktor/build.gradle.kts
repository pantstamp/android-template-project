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
            implementation(projects.core.logging.api)
            implementation(projects.core.network.api)

            // libraries
            implementation(projects.utils.koin)
            implementation(project.dependencies.platform(libs.ktor.bom))
            implementation(libs.bundles.ktor.common)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            // projects
            implementation(projects.core.logging.test)
            implementation(projects.core.serialization)
            implementation(projects.test.doubles.network)
            implementation(projects.utils.random)
            // libraries
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)

        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = namespaceWithProjectPackage(suffix = "network.ktor")
}
