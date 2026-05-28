plugins {
    id("com.android.application").version("7.4.2").apply(false)
    id("com.android.library").version("7.4.2").apply(false)
    kotlin("android").version("1.9.0").apply(false)
    kotlin("multiplatform").version("1.9.0").apply(false)
    id("com.google.devtools.ksp").version("1.9.0-1.0.13").apply(false)
}

buildscript {
    repositories {
        maven {
            url = uri("https://mirrors.tencent.com/repository/maven/kuikly")
        }
    }

    dependencies {
        classpath("com.tencent.kuikly:core-gradle-plugin:1.1.0.2")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
