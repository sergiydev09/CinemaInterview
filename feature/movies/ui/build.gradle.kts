plugins {
    id("ui-convention")
}

android {
    namespace = "com.cinema.movies.ui"
}

dependencies {
    implementation(projects.core.ui)
    implementation(projects.core.domain)
    implementation(projects.feature.movies.domain)

    // Navigation
    implementation(libs.bundles.navigation)

    // Image Loading
    implementation(libs.coil)
}
