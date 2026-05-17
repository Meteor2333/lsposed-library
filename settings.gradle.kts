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

rootProject.name = "lsposed-library"
include(":lsposed-api")
project(":lsposed-api").projectDir = file("api")
include(":lsposed-service")
project(":lsposed-service").projectDir = file("service")
include(":lsposed-example")
project(":lsposed-example").projectDir = file("example")