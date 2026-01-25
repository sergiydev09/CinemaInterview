pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Enable type-safe project accessors
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "CinemaInterview"

// App module
include(":app")

// Core modules
include(":core:data")
include(":core:domain")
include(":core:ui")
include(":core:features:favorites:data")
include(":core:features:favorites:domain")

// Feature modules - Login
include(":feature:login:data")
include(":feature:login:domain")
include(":feature:login:ui")

// Feature modules - Home
include(":feature:home:data")
include(":feature:home:domain")
include(":feature:home:ui")

// Feature modules - Movies
include(":feature:movies:data")
include(":feature:movies:domain")
include(":feature:movies:ui")

// Feature modules - People
include(":feature:people:data")
include(":feature:people:domain")
include(":feature:people:ui")
