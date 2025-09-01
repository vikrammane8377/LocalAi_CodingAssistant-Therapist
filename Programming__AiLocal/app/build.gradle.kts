plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("com.chaquo.python") version "15.0.1"
}

android {
    namespace = "com.example.programmingailocal"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.programmingailocal"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // Limit debug builds to a single ABI to reduce build size/time
        ndk {
            abiFilters.clear()
            abiFilters.add("arm64-v8a")
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}



dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.mediapipe.tasks.genai)
    implementation(libs.tflite)
    implementation(libs.tflite.gpu)

    // DataStore & WorkManager
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.work.runtime)

    // Navigation (for switching from “Start chat” → Chat screen)
    implementation(libs.androidx.navigation)

    // JSON serialization (used by config classes we’ll copy over)
    implementation(libs.kotlinx.serialization.json)
}