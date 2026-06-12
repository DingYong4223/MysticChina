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

// ── KuiklyUI 源码模块 (提供全平台依赖) ──
val kuiklyDir = file("../KuiklyUI")
val kuiklyBuildFile = "build.2.1.21.gradle.kts"

// :core 使用本地修改版 build 文件（去掉 js(IR) target，避免 duplicate 'clean' task 冲突）
include(":core")
project(":core").apply {
    projectDir = File(kuiklyDir, "core")
    buildFileName = kuiklyBuildFile
}

include(":core-annotations")
project(":core-annotations").apply {
    projectDir = File(kuiklyDir, "core-annotations")
    buildFileName = kuiklyBuildFile
}

include(":core-ksp")
project(":core-ksp").apply {
    projectDir = File(kuiklyDir, "core-ksp")
    buildFileName = kuiklyBuildFile
}

include(":core-render-android")
project(":core-render-android").apply {
    projectDir = File(kuiklyDir, "core-render-android")
    buildFileName = kuiklyBuildFile
}