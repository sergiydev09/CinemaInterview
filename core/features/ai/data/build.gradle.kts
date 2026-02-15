import java.util.Properties

plugins {
    id("data-convention")
}

android {
    namespace = "com.cinema.core.feature.ai.data"

    defaultConfig {
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }

        val openRouterApiKey = localProperties.getProperty("OPENROUTER_API_KEY")
            ?: System.getenv("OPENROUTER_API_KEY")
            ?: "test-key-for-ci"
        buildConfigField("String", "OPENROUTER_API_KEY", "\"$openRouterApiKey\"")
    }
}

dependencies {
    implementation(projects.core.features.ai.domain)
    implementation(projects.core.data)
}
