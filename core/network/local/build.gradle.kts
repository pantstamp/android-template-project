import com.sregs.utils.namespaceWithProjectPackage

plugins {
    id(libs.plugins.kmm.library.core.get().pluginId)
    id(libs.plugins.kmm.library.koin.get().pluginId)
}

// todo stelios find a way to easily add resources
kotlin {
    sourceSets {
        commonMain.dependencies {
            // projects
            implementation(projects.core.logging.api)
            implementation(projects.core.network.api)

            // libraries
            implementation(projects.utils.koin)
        }
        commonTest.dependencies {
            // libraries
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = namespaceWithProjectPackage(suffix = "network.local")
}
