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
        //字节Maven镜像仓库
        maven("https://artifact.bytedance.com/repository/Volcengine/")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        //字节Maven镜像仓库
        maven("https://artifact.bytedance.com/repository/Volcengine/")
    }
}

rootProject.name = "VoiceRobot"
include(":app")
include(":voiceengine")
include(":core")
include(":lottie")
include(":rtc")
