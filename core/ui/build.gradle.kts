plugins {
    id("ui-convention")
}

android {
    namespace = "com.cinema.core.ui"
}

dependencies {
    // Core domain module
    implementation(projects.core.domain)
}
