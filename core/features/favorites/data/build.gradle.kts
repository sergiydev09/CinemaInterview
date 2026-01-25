plugins {
    id("data-convention")
}

android {
    namespace = "com.cinema.core.favorites.data"
}

dependencies {
    implementation(projects.core.features.favorites.domain)
    implementation(projects.core.data)
}
