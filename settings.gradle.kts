
include(":ksemodule")
project(":ksemodule").projectDir = File(settingsDir, "../KSolarEdge/ksemodule")

include(":MyEnergiKlient")
project(":MyEnergiKlient").projectDir = File(settingsDir, "../MyEnergiKlient/MyEnergiKlient")

include(":EcoForestKlient")
project(":EcoForestKlient").projectDir = File(settingsDir, "../EcoForestKlient/EcoForestKlient")

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

rootProject.name = "EnergyHub"
include(":app")
 