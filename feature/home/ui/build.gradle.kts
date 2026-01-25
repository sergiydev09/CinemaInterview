plugins {
    id("ui-convention")
}

android {
    namespace = "com.cinema.home.ui"
}

dependencies {
    implementation(projects.core.ui)
    implementation(projects.core.domain)
    implementation(projects.core.features.favorites.domain)
    implementation(projects.feature.home.domain)
}
