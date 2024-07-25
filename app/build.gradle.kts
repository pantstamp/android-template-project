import com.pantelisstampoulis.configuration.FlavorProperty
import com.pantelisstampoulis.utils.getFlavoredModule
import java.util.Properties


plugins {
    id(libs.plugins.custom.application.compose.get().pluginId)
}

android {
    namespace = "com.pantelisstampoulis.androidtemplateproject"

    defaultConfig {
        applicationId = "com.pantelisstampoulis.androidtemplateproject"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(projects.core.di)
    implementation(getFlavoredModule(property = FlavorProperty.Network))
    implementation(getFlavoredModule(property = FlavorProperty.Database))
    implementation(projects.core.domain)
    implementation(projects.core.logging.api)
    implementation(getFlavoredModule(property = FlavorProperty.Logging))
    implementation(projects.presentation.mvi)
    implementation(projects.presentation.viewmodel)
    implementation(projects.core.data)

    implementation(platform(libs.koin.bom))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}