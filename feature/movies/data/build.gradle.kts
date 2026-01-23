plugins {
    id("data-convention")
}

android {
    namespace = "com.cinema.movies.data"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.domain)
    implementation(projects.feature.movies.domain)

    // Networking
    implementation(libs.retrofit.core)
    implementation(libs.moshi.core)
    ksp(libs.moshi.codegen)
}
