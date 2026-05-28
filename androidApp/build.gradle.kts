plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.yijian.android"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.yijian.android"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        doNotStrip("**/*.so")
    }
    sourceSets.getByName("main") {
        jniLibs {
            srcDir("libs")
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":shared"))
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.10.0")
}
