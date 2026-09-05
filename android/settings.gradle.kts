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

rootProject.name = "mushaf"

include(":app")
include(":core:common")
include(":core:designsystem")
include(":core:database")
include(":core:datastore")
include(":core:data")
include(":core:domain")
include(":feature:reader")
include(":feature:navigation")
include(":feature:search")
include(":feature:settings")
