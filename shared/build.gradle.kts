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

// ── 检测是否使用 KuiklyUI 源码构建 ──
// settings.full.gradle.kts 中 include 了 KuiklyUI 源码项目 (:core-annotations 等)，
// 此时可用本地源码替代已发布 AAR/Framework，支持 macOS 等未发布平台
val isSourceBuild = rootProject.findProject(":core-annotations") != null

kotlin {
    // ── Android ──────────────────────────────────────────
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "21"
            }
        }
    }

    // ── iOS ──────────────────────────────────────────────
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    // ── macOS — 仅在源码构建模式下启用 ──────────────────
    // 已发布的 com.tencent.kuikly-open:* 不含 macOS artifact，
    // 必须使用 KuiklyUI 源码构建 (settings.full.gradle.kts)
    if (isSourceBuild) {
        macosX64()
        macosArm64()

        // ── JS / Web — 源码构建时可选启用 ──────────────────
        // js(IR) { browser(); binaries.executable() }
    }

    // ── CocoaPods ────────────────────────────────────────
    cocoapods {
        summary = "MysticChina Shared Module"
        homepage = "https://github.com/DingYong4223/MysticChina"
        version = "1.0"
        ios.deploymentTarget = "14.1"
        if (isSourceBuild) {
            osx.deploymentTarget = "10.13"
        }
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

        // iOS source sets (always included)
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }

        // appleMain: shared between iOS & macOS (always declared, used by targets that exist)
        val appleMain by creating {
            dependsOn(commonMain)
            iosMain.dependsOn(this)
        }

        // macOS source sets — 仅源码构建模式下存在
        if (isSourceBuild) {
            val macosX64Main by getting {
                dependsOn(appleMain)
            }
            val macosArm64Main by getting {
                dependsOn(appleMain)
            }
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
        if (isSourceBuild) {
            val macosX64Test by getting
            val macosArm64Test by getting
            val macosTest by creating {
                dependsOn(commonTest)
                macosX64Test.dependsOn(this)
                macosArm64Test.dependsOn(this)
            }
        }
    }

    // ── Native 平台编译器参数 ─────────────────────────────
    targets.withType<KotlinNativeTarget> {
        val mainSourceSets = this.compilations.getByName("main").defaultSourceSet
        when {
            konanTarget.family.isAppleFamily -> {
                mainSourceSets.dependsOn(sourceSets.getByName("appleMain"))
            }
        }
    }
}

// ── KSP 配置 ─────────────────────────────────────────────
dependencies {
    compileOnly("com.tencent.kuikly-open:core-ksp:2.0.0") {
        add("kspAndroid", this)
        add("kspIosArm64", this)
        add("kspIosX64", this)
        add("kspIosSimulatorArm64", this)
        if (isSourceBuild) {
            add("kspMacosArm64", this)
            add("kspMacosX64", this)
        }
    }
}

// KSP metadata workaround — Kuikly KSP 生成 Android 风格入口类到 commonMain metadata
// 但 metadata 源集不可见 IKuiklyCoreEntry (仅 Android 已提供)
tasks.matching { it.name == "compileCommonMainKotlinMetadata" }.configureEach {
    doFirst {
        layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin").get().asFile
            .deleteRecursively()
    }
}

// ── Android 配置 ─────────────────────────────────────────
android {
    namespace = "com.fula.mysticchina.shared"
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
