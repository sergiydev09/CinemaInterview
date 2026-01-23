plugins {
    id("ui-convention")
}

android {
    namespace = "com.cinema.login.ui"
}

dependencies {
    implementation(projects.core.ui)
    implementation(projects.core.domain)
    implementation(projects.feature.login.domain)

    // Testing
    testImplementation(libs.bundles.testing)
    androidTestImplementation(libs.bundles.androidTesting)
}
