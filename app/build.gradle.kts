plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "app.qiap"
    compileSdk = 37

    defaultConfig {
        applicationId = "app.qiap"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // MediaPipe ships ~10 MB of native code per ABI. Real phones are ARM; x86_64 is added
        // for debug only so the emulator works. Keeps the release APK inside the 40 MB budget.
        ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a") }
    }

    // MediaPipe memory-maps the model; a compressed asset would have to be copied first.
    androidResources { noCompress += "task" }

    buildTypes {
        debug {
            ndk { abiFilters += "x86_64" }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // TODO(release): replace with the upload key before any store build.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    // Navigation 3: back stack is plain state we own, which suits a 7-screen app and the alarm deep-link into Ringing.
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    // Required by Navigation 3 to save/restore typed route keys.
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.androidx.profileinstaller)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // CameraX: camera preview + per-frame analysis with lifecycle binding (CLAUDE.md stack).
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    // Compose viewfinder for the preview, so the workout screen stays pure Compose.
    implementation(libs.androidx.camera.compose)
    // On-device pose landmarks (CLAUDE.md stack), behind the PoseEngine interface.
    implementation(libs.mediapipe.tasks.vision)

    baselineProfile(project(":baselineprofile"))

    testImplementation(libs.junit)
}
