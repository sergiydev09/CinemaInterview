plugins {
    id("domain-convention")
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.features.favorites.domain)
}
