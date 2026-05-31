plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    `maven-publish`
}

android {
    namespace = "io.github.hanhyo.composemindmap"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "io.github.hanhyo"
                artifactId = "compose-mindmap"
                version = "0.1.1"

                pom {
                    name.set("Compose MindMap")
                    description.set("Jetpack Compose canvas-based mind map library")
                    url.set("https://github.com/UiHyeon-Kim/compose-mindmap")
                    licenses {
                        license {
                            name.set("Apache-2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0")
                        }
                    }
                    developers {
                        developer {
                            id.set("UiHyeon-Kim")
                            name.set("김의현")
                        }
                    }
                    scm {
                        connection.set("scm:git:github.com/UiHyeon-Kim/compose-mindmap.git")
                        developerConnection.set("scm:git:ssh://github.com/UiHyeon-Kim/compose-mindmap.git")
                        url.set("https://github.com/UiHyeon-Kim/compose-mindmap")
                    }
                }
            }
        }
    }
}

dependencies {
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.ui)
    api(libs.androidx.ui.graphics)
    api("androidx.compose.foundation:foundation")
    api("androidx.compose.foundation:foundation-layout")
    implementation(libs.kotlinx.coroutines.core)

    debugImplementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
}
