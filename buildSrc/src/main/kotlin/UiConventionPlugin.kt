import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

class UiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("com.google.dagger.hilt.android")
                apply("com.google.devtools.ksp")
                apply("org.jetbrains.kotlin.plugin.compose")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            extensions.configure<LibraryExtension> {
                compileSdk = 36

                defaultConfig {
                    minSdk = 26
                    consumerProguardFiles("consumer-rules.pro")
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }

                buildFeatures {
                    compose = true
                }
            }

            extensions.configure<ComposeCompilerGradlePluginExtension> {
                enableStrongSkippingMode.set(true)
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            dependencies {
                add("implementation", libs.findLibrary("androidx-core-ktx").get())
                add("implementation", libs.findLibrary("androidx-appcompat").get())
                add("implementation", libs.findLibrary("material").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-ktx").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-runtime-ktx").get())
                add("implementation", libs.findLibrary("hilt-android").get())
                add("ksp", libs.findLibrary("hilt-compiler").get())
                add("implementation", libs.findLibrary("kotlinx-coroutines-core").get())
                add("implementation", libs.findLibrary("kotlinx-coroutines-android").get())

                // Compose
                add("implementation", platform(libs.findLibrary("compose-bom").get()))
                add("implementation", libs.findBundle("compose").get())
                add("debugImplementation", libs.findBundle("compose-debug").get())

                // Coil Compose
                add("implementation", libs.findLibrary("coil-compose").get())
                add("implementation", libs.findLibrary("coil-network-okhttp").get())

                // Kotlin Serialization
                add("implementation", libs.findLibrary("kotlinx-serialization-json").get())

                add("testImplementation", libs.findBundle("testing").get())
            }
        }
    }
}
