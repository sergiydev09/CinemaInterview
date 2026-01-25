plugins {
    id("data-convention")
}

android {
    namespace = "com.cinema.people.data"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.domain)
    implementation(projects.feature.people.domain)

    // Networking
    implementation(libs.retrofit.core)
}
