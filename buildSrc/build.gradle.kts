plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
    implementation(libs.hilt.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("dataConvention") {
            id = "data-convention"
            implementationClass = "DataConventionPlugin"
        }
        register("uiConvention") {
            id = "ui-convention"
            implementationClass = "UiConventionPlugin"
        }
        register("domainConvention") {
            id = "domain-convention"
            implementationClass = "DomainConventionPlugin"
        }
    }
}
