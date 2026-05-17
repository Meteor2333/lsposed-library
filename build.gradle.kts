// Top-level build file where you can add configuration options common to all sub-projects/modules.
tasks.register<Delete>("clean") {
    description = "Deletes the build directory."
    delete(rootProject.layout.buildDirectory)
}