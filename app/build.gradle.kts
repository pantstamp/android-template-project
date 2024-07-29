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

    // features
    implementation(projects.feature.movieCatalog)
}