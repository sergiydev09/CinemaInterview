// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Plugins are provided by buildSrc convention plugins

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
