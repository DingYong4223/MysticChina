plugins {
    id("com.android.application").version("8.2.2").apply(false)
    id("com.android.library").version("8.2.2").apply(false)
    kotlin("android").version("1.9.22").apply(false)
    kotlin("multiplatform").version("1.9.22").apply(false)
    id("com.google.devtools.ksp").version("1.9.22-1.0.17").apply(false)
}

buildscript {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/")
        }
    }

    dependencies {
        classpath("com.tencent.kuikly-open:core-gradle-plugin:2.4.0-1.9.22")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
