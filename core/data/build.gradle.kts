plugins {
    id("data-convention")
}

android {
    namespace = "com.cinema.core.data"
}

dependencies {
    // Core domain module
    api(projects.core.domain)

    // Networking
    api(libs.bundles.networking)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Security - Tink for encryption
    implementation(libs.tink.android)
}
