plugins {
    id("ui-convention")
}

android {
    namespace = "com.cinema.core.ai.ui"
}

dependencies {
    implementation(projects.core.ai.domain)
    implementation(projects.core.ui)
    implementation(libs.compose.material.icons.extended)
}
