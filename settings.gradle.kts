pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven {
            url = uri("https://mirrors.tencent.com/repository/maven/thirdparty")
        }
        maven {
            url = uri("https://mirrors.tencent.com/repository/maven/kuikly")
        }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://mirrors.tencent.com/repository/maven/thirdparty")
        }
        maven {
            url = uri("https://mirrors.tencent.com/repository/maven/kuikly")
        }
        maven {
            url = uri("https://jitpack.io")
        }
    }
}

rootProject.name = "yijian"
include(":androidApp")
include(":shared")
