plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("com.google.devtools.ksp")
    id("com.tencent.kuikly-open.kuikly")
}

val kuikly_version = "2.4.0-1.9.22"

repositories {
    google()
    mavenCentral()
    maven {
        url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/")
    }
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Yijian Video Editor Shared Module"
        homepage = "https://github.com/yijian"
        version = "1.0"
        ios.deploymentTarget = "14.1"
        // podfile = project.file("../iosApp/Podfile") // Use :path reference in iosApp/Podfile instead
        framework {
            baseName = "shared"
        }
        extraSpecAttributes["resources"] = "['src/commonMain/assets/**']"
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.tencent.kuikly-open:core:${kuikly_version}")
                implementation("com.tencent.kuikly-open:core-annotations:${kuikly_version}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                api("com.tencent.kuikly-open:core-render-android:${kuikly_version}")
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

group = "com.yijian"
version = "1.0.0"

dependencies {
    compileOnly("com.tencent.kuikly-open:core-ksp:${kuikly_version}") {
        add("kspAndroid", this)
        add("kspIosArm64", this)
        add("kspIosX64", this)
        add("kspIosSimulatorArm64", this)
    }
}

// KSP generates KuiklyCoreEntry.kt in commonMain metadata referencing android-only IKuiklyCoreEntry.
// This file is not needed (platform-specific compilations generate their own entry files).
tasks.matching { it.name == "compileCommonMainKotlinMetadata" }.configureEach {
    doFirst {
        layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin").get().asFile
            .deleteRecursively()
    }
}

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
