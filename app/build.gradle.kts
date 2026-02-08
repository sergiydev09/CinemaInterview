plugins {
    id("com.android.application")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
//    id("io.github.sergiydev09.mockkhttp") version "1.4.30"
}

android {
    namespace = "com.cinema.interview"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cinema.interview"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

composeCompiler {
    enableStrongSkippingMode = true
}

dependencies {
    // Core modules
    implementation(projects.core.data)
    implementation(projects.core.domain)
    implementation(projects.core.ui)
    implementation(projects.core.ai.data)
    implementation(projects.core.ai.domain)
    implementation(projects.core.ai.ui)
    implementation(projects.core.features.favorites.data)
    implementation(projects.core.features.favorites.domain)

    // Feature modules
    implementation(projects.feature.login.ui)
    implementation(projects.feature.login.data)
    implementation(projects.feature.home.ui)
    implementation(projects.feature.home.data)
    implementation(projects.feature.movies.ui)
    implementation(projects.feature.movies.data)
    implementation(projects.feature.people.ui)
    implementation(projects.feature.people.data)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    debugImplementation(libs.bundles.compose.debug)

    // Coil Compose
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Kotlin Serialization
    implementation(libs.kotlinx.serialization.json)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Testing
    testImplementation(libs.bundles.testing)
}
