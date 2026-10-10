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
                jvmTarget = "21"
            }
        }
    }

    // ── iOS ──────────────────────────────────────────────
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    // KuiklyUI 2.28.0 provides macOS in both source and Maven modes.
    macosX64()
    macosArm64()

    // ── JS / Web — 源码构建时可选启用 ──────────────────
    // js(IR) { browser(); binaries.executable() }

    // ── CocoaPods ────────────────────────────────────────
    cocoapods {
        summary = "MysticChina Shared Module"
        homepage = "https://github.com/DingYong4223/MysticChina"
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
                // 源码联调由 debug_aar_config.json 替换同一 Maven 依赖。
                implementation("com.fula:fula-page:1.0.0")
                implementation("com.tencent.kuikly-open:core:2.0.0")
                implementation("com.tencent.kuikly-open:core-annotations:2.0.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-KBA-001")
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
                implementation("com.tencent.kuiklyx-open:coroutines:1.5.0-2.0.21") {
                    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
                }
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
            dependencies {
                implementation("com.tencent.kuiklyx-open:coroutines:1.5.0-2.0.21") {
                    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
                }
            }
        }

        // appleMain: shared between iOS & macOS (always declared, used by targets that exist)
        val appleMain by creating {
            dependsOn(commonMain)
            iosMain.dependsOn(this)
        }

        // macOS source sets
        val macosX64Main by getting {
            dependsOn(appleMain)
        }
        val macosArm64Main by getting {
            dependsOn(appleMain)
        }
        val macosMain by creating {
            dependsOn(appleMain)
            macosX64Main.dependsOn(this)
            macosArm64Main.dependsOn(this)
            dependencies { implementation("org.jetbrains.kotlinx:atomicfu:0.23.2") }
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
        when {
            konanTarget.family.isAppleFamily -> {
                mainSourceSets.dependsOn(sourceSets.getByName("appleMain"))
            }
        }
    }
}

// ── KSP 配置 ─────────────────────────────────────────────
// shared 是 Kuikly 主模块，负责汇总自己与子模块（:fula-page，moduleId = fulaPage）的 @Page 注册。
// fula-page 声明为子模块（isMainModule = false），自身不生成
// com.tencent.kuikly.core.android.KuiklyCoreEntry，只生成 KuiklyCoreEntry_fulaPage 与
// KuiklyPages_fulaPage；主模块汇总后，子模块的 ProtocolPage 才会被注册。
// 详见 KuiklyUI docs/DevGuide/multi_module.md。
ksp {
    arg("enableMultiModule", "true")
    arg("isMainModule", "true")
    arg("moduleId", "shared")
    arg("subModules", "fulaPage")
}

dependencies {
    compileOnly("com.tencent.kuikly-open:core-ksp:2.0.0") {
        add("kspAndroid", this)
        add("kspIosArm64", this)
        add("kspIosX64", this)
        add("kspIosSimulatorArm64", this)
        add("kspMacosArm64", this)
        add("kspMacosX64", this)
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
