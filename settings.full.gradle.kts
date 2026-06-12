pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven {
            url = uri("https://mirrors.tencent.com/repository/maven-tencent/")
        }
        maven {
            url = uri("https://mirrors.tencent.com/nexus/repository/gradle-plugins/")
        }
    }
}

// ── KuiklyUI 源码模式 settings ──────────────
// 使用方式: cp settings.full.gradle.kts settings.gradle.kts
// 启用 macOS / JS 等未发布平台 (需 KuiklyUI 源码构建)
// 默认 settings.gradle.kts 使用已发布 Maven library (仅 iOS + Android)

rootProject.buildFileName = "build.gradle.kts"

// 源码模式通过 rootProject.findProject(":core-annotations") != null 自动检测

// ── KuiklyUI 源码模块 ──
val kuiklyBuildFileName = "build.2.1.21.gradle.kts"

include(":core-annotations")
project(":core-annotations").projectDir = File(rootDir, "../KuiklyUI/core-annotations")
project(":core-annotations").buildFileName = kuiklyBuildFileName

include(":core-ksp")
project(":core-ksp").projectDir = File(rootDir, "../KuiklyUI/core-ksp")
project(":core-ksp").buildFileName = kuiklyBuildFileName

include(":core")
project(":core").projectDir = File(rootDir, "../KuiklyUI/core")
project(":core").buildFileName = kuiklyBuildFileName

include(":core-render-android")
project(":core-render-android").projectDir = File(rootDir, "../KuiklyUI/core-render-android")
project(":core-render-android").buildFileName = kuiklyBuildFileName

// compose 模块需要独立的 kotlin 插件，且 MysticChina 项目不使用 Compose
// include(":compose")
// project(":compose").projectDir = File(rootDir, "../KuiklyUI/compose")
// project(":compose").buildFileName = kuiklyBuildFileName

// ── MysticChina 模块 ────────────────────────────
include(":shared")
include(":androidApp")