// ============================================================
// MysticChina — All-platform root build (upgraded to Kotlin 2.1.21)
// ============================================================

plugins {
    id("com.android.application").version("8.6.1").apply(false)
    id("com.android.library").version("8.6.1").apply(false)
    kotlin("android").version("2.1.21").apply(false)
    kotlin("multiplatform").version("2.1.21").apply(false)
    id("com.google.devtools.ksp").version("2.1.21-2.0.1").apply(false)
    id("com.squareup.wire").version("5.5.1").apply(false)
}

buildscript {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/")
        }
    }
    dependencies {
        classpath("com.tencent.kuikly-open:core-gradle-plugin:2.14.1-2.0.21")
    }
}

allprojects {
    if (path in listOf(":core", ":core-annotations")) {
        plugins.withId("org.jetbrains.kotlin.native.cocoapods") {
            val kotlin = extensions.getByType<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>()
            (kotlin as org.gradle.api.plugins.ExtensionAware).extensions
                .getByType<org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension>().noPodspec()
        }
    }
    if (version == Project.DEFAULT_VERSION) {
        version = "1.0.0"
    }

    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven {
            url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/")
        }
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
        jvmTargetValidationMode.set(
            org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode.WARNING
        )
    }
}

// ── Clean task ───────────────────────────────────────────────
// Apply LifecycleBasePlugin first so NodeJsRootPlugin (triggered by js(IR){nodejs()})
// finds 'clean' already registered and skips duplicate registration.
// This is required because ../KuiklyUI/core/build.kuikly-core.gradle.kts includes
// js(IR){nodejs()} which applies LifecycleBasePlugin for all projects using that build file.
apply(plugin = "lifecycle-base")
tasks.named<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
