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
<<<<<<< HEAD
=======
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
>>>>>>> 96e39c8 (Initial commit)




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
