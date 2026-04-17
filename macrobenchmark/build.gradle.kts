plugins {
    id("com.android.library")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.benchmark)
}

android {
    namespace = "com.sample.paymenttransfer.macrobenchmark"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
        
        testInstrumentationRunner = "androidx.benchmark.runner.BenchmarkJUnitRunner"
    }
    
    testOptions {
        targetSdk = 36
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

dependencies {
    androidTestImplementation(libs.androidx.benchmark.macro.junit4)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.androidx.runner)
}