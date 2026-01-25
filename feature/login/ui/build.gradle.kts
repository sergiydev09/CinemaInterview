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
}
