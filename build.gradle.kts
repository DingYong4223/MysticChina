// ============================================================
// MysticChina — All-platform root build (upgraded to Kotlin 2.1.21)
// ============================================================

plugins {
    id("com.android.application").version("8.2.2").apply(false)
    id("com.android.library").version("8.2.2").apply(false)
    kotlin("android").version("2.1.21").apply(false)
    kotlin("multiplatform").version("2.1.21").apply(false)
    id("com.google.devtools.ksp").version("2.1.21-2.0.1").apply(false)
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
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/")
        }
    }
    configurations.all {
        resolutionStrategy.dependencySubstitution {
            substitute(module("com.tencent.kuikly-open:core"))
                .using(project(":core"))
                .because("Use local KuiklyUI source for all-platform support")
            substitute(module("com.tencent.kuikly-open:core-annotations"))
                .using(project(":core-annotations"))
            substitute(module("com.tencent.kuikly-open:core-ksp"))
                .using(project(":core-ksp"))
            substitute(module("com.tencent.kuikly-open:core-render-android"))
                .using(project(":core-render-android"))
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