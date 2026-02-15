plugins {
    id("ui-convention")
}

android {
    namespace = "com.cinema.core.feature.ai.ui"
}

dependencies {
    implementation(projects.core.features.ai.domain)
    implementation(projects.core.ui)
    implementation(libs.compose.material.icons.extended)
}
