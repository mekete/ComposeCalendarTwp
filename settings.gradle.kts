pluginManagement {
    repositories {
        google()
        maven {
            url = uri("https://company/com/maven2")
        }
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}




dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://company/com/maven2")
        }
        mavenLocal()
        flatDir {
            dirs("libs")
        }
    }
}

rootProject.name = "Ethiopian Calendar"
include(":app")
