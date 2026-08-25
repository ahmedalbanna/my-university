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

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "Jamaaty"

include(":app")
// P4-1: future modularization — uncomment when extracting (see MODULARIZATION.md)
// include(":core:data")
// include(":core:ui")
// include(":feature:library")
// include(":feature:wallet")
// include(":feature:gpa")
// include(":feature:schedule")
// include(":feature:translate")
