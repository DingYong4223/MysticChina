// ============================================================
// mysticchina — All-platform settings (includes KuiklyUI source modules)
// ============================================================

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
        maven {
            url = uri("https://mirrors.tencent.com/nexus/repository/gradle-plugins/")
        }
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
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

rootProject.name = "mysticchina"
include(":androidApp")
include(":shared")

apply(from = "debug_aar.gradle")
