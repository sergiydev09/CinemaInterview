import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class DataConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("com.google.dagger.hilt.android")
                apply("com.google.devtools.ksp")
                apply("org.jetbrains.kotlin.plugin.serialization")
                apply("jacoco")
            }

            extensions.configure<LibraryExtension> {
                compileSdk = 36

                defaultConfig {
                    minSdk = 26
                    consumerProguardFiles("consumer-rules.pro")
                }

                buildFeatures {
                    buildConfig = true
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }

                testOptions {
                    unitTests.isReturnDefaultValues = true
                    unitTests.all {
                        it.extensions.configure(org.gradle.testing.jacoco.plugins.JacocoTaskExtension::class.java) {
                            isIncludeNoLocationClasses = true
                            excludes = listOf("jdk.internal.*")
                        }
                    }
                }
            }

            extensions.configure<KotlinAndroidProjectExtension> {
                compilerOptions {
                    freeCompilerArgs.addAll(
                        "-opt-in=kotlinx.serialization.InternalSerializationApi"
                    )
                }
            }

            extensions.configure<JacocoPluginExtension> {
                toolVersion = "0.8.11"
            }

            afterEvaluate {
                tasks.register("jacocoTestReport", JacocoReport::class.java) {
                    dependsOn("testDebugUnitTest")

                    reports {
                        xml.required.set(true)
                        html.required.set(true)
                    }

                    val fileFilter = listOf(
                        "**/R.class",
                        "**/R\$*.class",
                        "**/BuildConfig.*",
                        "**/Manifest*.*",
                        "**/*Test*.*",
                        "android/**/*.*",
                        "**/*_Hilt*.*",
                        "**/Hilt_*.*",
                        "**/*_Factory.*",
                        "**/*_MembersInjector.*",
                        "**/hilt_aggregated_deps/**"
                    )

                    val buildDirectory = layout.buildDirectory.asFile.get()
                    val debugTree = fileTree("$buildDirectory/intermediates/runtime_library_classes_dir/debug/bundleLibRuntimeToDirDebug") {
                        exclude(fileFilter)
                    }

                    val mainSrc = "$projectDir/src/main/kotlin"

                    sourceDirectories.setFrom(files(mainSrc))
                    classDirectories.setFrom(files(debugTree))
                    executionData.setFrom(fileTree(buildDirectory) {
                        include("jacoco/testDebugUnitTest.exec")
                    })
                }
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            dependencies {
                add("implementation", libs.findLibrary("hilt-android").get())
                add("ksp", libs.findLibrary("hilt-compiler").get())
                add("implementation", libs.findLibrary("kotlinx-coroutines-core").get())
                add("implementation", libs.findLibrary("kotlinx-coroutines-android").get())
                add("implementation", libs.findLibrary("kotlinx-serialization-json").get())
                add("testImplementation", libs.findBundle("testing").get())
            }
        }
    }
}
