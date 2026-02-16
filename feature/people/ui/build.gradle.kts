plugins {
    id("ui-convention")
}

android {
    namespace = "com.cinema.people.ui"
}

dependencies {
    implementation(projects.core.ui)
    implementation(projects.core.domain)
    implementation(projects.core.features.ai.domain)
    implementation(projects.feature.people.domain)
}
