pluginManagement {
    plugins {
        id("com.tencent.kuikly-open.kuikly") version "2.4.0-1.9.22"
    }
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven {
            url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/")
        }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/")
        }
        maven {
            url = uri("https://jitpack.io")
        }
    }
}

rootProject.name = "yijian"
include(":androidApp")
include(":shared")
