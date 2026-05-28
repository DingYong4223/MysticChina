import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("com.google.devtools.ksp")
    id("com.tencent.kuikly-open.kuikly")
}

repositories {
    google()
    mavenCentral()
    maven {
        url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/")
    }
}

kotlin {
    // ── Android ──────────────────────────────────────────
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    // ── iOS ──────────────────────────────────────────────
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    // ── macOS ────────────────────────────────────────────
    macosX64()
    macosArm64()

    // ── Web / JS ─────────────────────────────────────────
    // js(IR) { ... }  — disabled: webpack clean task conflict with AGP 8.x.
    // Enable when AGP upgraded or using separate JS subproject.

    // ── CocoaPods ────────────────────────────────────────
    cocoapods {
        summary = "Yijian Video Editor Shared Module"
        homepage = "https://github.com/yijian"
        version = "1.0"
        ios.deploymentTarget = "14.1"
        osx.deploymentTarget = "10.13"
        framework {
            baseName = "shared"
            isStatic = true
        }
        license = "MIT"
        extraSpecAttributes["resources"] = "['src/commonMain/assets/**']"
    }

    // ── 源集配置 ─────────────────────────────────────────
    sourceSets {
        val commonMain by getting {
            dependencies {
                // 版本号可任意填写 — root build 中的 dependencySubstitution
                // 会将 Maven 依赖替换为本地 KuiklyUI 源码 project 模块
                implementation("com.tencent.kuikly-open:core:2.0.0")
                implementation("com.tencent.kuikly-open:core-annotations:2.0.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        // Android
        val androidMain by getting {
            dependencies {
                api("com.tencent.kuikly-open:core-render-android:2.0.0")
            }
        }

        // iOS source sets
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }

        // macOS source sets
        val macosX64Main by getting
        val macosArm64Main by getting

        // appleMain: shared between iOS & macOS
        val appleMain by creating {
            dependsOn(commonMain)
            iosMain.dependsOn(this)
            macosX64Main.dependsOn(this)
            macosArm64Main.dependsOn(this)
        }

        // JS / Web source set — 需启用 js(IR) target 后取消注释
        // val jsMain by getting { dependsOn(commonMain) }

        // Test source sets
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
        val macosX64Test by getting
        val macosArm64Test by getting
        val macosTest by creating {
            dependsOn(commonTest)
            macosX64Test.dependsOn(this)
            macosArm64Test.dependsOn(this)
        }
    }

    // ── Native 平台编译器参数 ─────────────────────────────
    targets.withType<KotlinNativeTarget> {
        val mainSourceSets = this.compilations.getByName("main").defaultSourceSet
        when (konanTarget.family) {
            Family.OSX -> mainSourceSets.dependsOn(sourceSets.getByName("appleMain"))
            else -> if (konanTarget.family.isAppleFamily) {
                mainSourceSets.dependsOn(sourceSets.getByName("appleMain"))
            }
        }
    }
}

group = "com.yijian"
version = "1.0.0"

// ── KSP 配置 ─────────────────────────────────────────────
dependencies {
    compileOnly("com.tencent.kuikly-open:core-ksp:2.0.0") {
        add("kspAndroid", this)
        add("kspIosArm64", this)
        add("kspIosX64", this)
        add("kspIosSimulatorArm64", this)
        add("kspMacosArm64", this)
        add("kspMacosX64", this)
        // add("kspJs", this)  // JS target 需先启用
    }
}

// KSP metadata workaround — see https://github.com/Tencent-TDS/KuiklyUI
tasks.matching { it.name == "compileCommonMainKotlinMetadata" }.configureEach {
    doFirst {
        layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin").get().asFile
            .deleteRecursively()
    }
}

// ── Android 配置 ─────────────────────────────────────────
android {
    namespace = "com.yijian"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
        targetSdk = 34
    }
    sourceSets {
        named("main") {
            jniLibs.srcDirs("src/androidMain/libs/")
            assets.srcDirs("src/commonMain/assets")
        }
    }
}