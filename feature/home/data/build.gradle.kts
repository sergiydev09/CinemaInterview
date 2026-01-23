plugins {
    id("data-convention")
}

android {
    namespace = "com.cinema.home.data"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.domain)
    implementation(projects.feature.home.domain)
}
