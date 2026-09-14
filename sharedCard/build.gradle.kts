plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    androidTarget {
        compilations.all { kotlinOptions.jvmTarget = "21" }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    if (rootProject.findProject(":core-annotations") != null) {
        macosX64()
        macosArm64()
    }

    sourceSets {
        commonMain.dependencies {
            implementation("com.tencent.kuikly-open:core:2.0.0")
        }
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}

android {
    namespace = "com.fula.mysticchina.sharedcard"
    compileSdk = 34
    defaultConfig { minSdk = 21 }
}
