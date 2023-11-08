pluginManagement {
    repositories {
        google()
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        mavenCentral()
    }
}
rootProject.name = "Chitchat"
include(":app")
 