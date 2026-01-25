import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class DomainConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.jvm")
                apply("jacoco")
            }

            extensions.configure<KotlinJvmProjectExtension> {
                jvmToolchain(17)
            }

            extensions.configure<JacocoPluginExtension> {
                toolVersion = "0.8.11"
            }

            tasks.withType(JacocoReport::class.java) {
                reports {
                    xml.required.set(true)
                    html.required.set(true)
                }
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            dependencies {
                add("implementation", libs.findLibrary("kotlinx-coroutines-core").get())
                add("implementation", libs.findLibrary("javax-inject").get())
                add("testImplementation", libs.findBundle("testing").get())
            }
        }
    }
}
