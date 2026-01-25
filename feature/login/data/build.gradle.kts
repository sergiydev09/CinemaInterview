import java.util.Properties

plugins {
    id("data-convention")
}

android {
    namespace = "com.cinema.login.data"

    defaultConfig {
        // Load TMDB API token from local.properties
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }

        val tmdbToken = localProperties.getProperty("TMDB_API_TOKEN")
            ?: System.getenv("TMDB_API_TOKEN")
            ?: "test-token-for-ci"
        buildConfigField("String", "TMDB_API_TOKEN", "\"$tmdbToken\"")
    }
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.domain)
    implementation(projects.feature.login.domain)

    // Testing
    testImplementation(libs.bundles.testing)
}
