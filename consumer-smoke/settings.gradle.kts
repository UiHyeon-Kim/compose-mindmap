import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        if (providers.gradleProperty("mindMapRepository").orNull == "mavenLocal") mavenLocal()
        maven("https://jitpack.io")
    }
}
rootProject.name = "ComposeMindMapConsumerSmoke"
include(":app")
