plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.libxposed.api"
    compileSdk = 36
    buildToolsVersion = "36.1.0"
    androidResources.enable = false

    defaultConfig {
        minSdk = 24
    }

    buildFeatures {
        buildConfig = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    compileOnly(libs.annotation)
}