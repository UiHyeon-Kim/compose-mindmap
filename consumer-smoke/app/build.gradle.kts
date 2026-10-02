plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "io.github.hanhyo.composemindmap.consumer"
    compileSdk = 36
    defaultConfig {
        applicationId = "io.github.hanhyo.composemindmap.consumer"
        minSdk = 26
        targetSdk = 36
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
}

kotlin {
    compilerOptions { jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17 }
}

dependencies {
    val mindMapVersion = providers.gradleProperty("mindMapVersion").orElse("0.2.0").get()
    implementation("com.github.UiHyeon-Kim:compose-mindmap:$mindMapVersion")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation(platform("androidx.compose:compose-bom:2026.03.01"))
    implementation("androidx.compose.ui:ui")
}
