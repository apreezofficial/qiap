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
        // CI passes its run number so every uploaded bundle has a higher code than the last.
        versionCode = (System.getenv("QIAP_VERSION_CODE") ?: "1").toInt()
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // MediaPipe ships ~10 MB of native code per ABI. Real phones are ARM; x86_64 is added
        // for debug only so the emulator works. Keeps the release APK inside the 40 MB budget.
        ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a") }
    }

    // MediaPipe memory-maps the model; a compressed asset would have to be copied first.
    androidResources { noCompress += "task" }

    // Store builds are signed with the upload key, which is never in the repo: CI decodes it from a
    // secret and exports these variables (docs/RELEASE.md). Without them, release falls back to the
    // debug key so the APK still installs for testing, but Play will not accept it.
    signingConfigs {
        val keystore = System.getenv("QIAP_KEYSTORE_FILE")
        if (keystore != null && file(keystore).isFile) {
            create("upload") {
                storeFile = file(keystore)
                storePassword = System.getenv("QIAP_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("QIAP_KEY_ALIAS")
                keyPassword = System.getenv("QIAP_KEY_PASSWORD")
            }
        }
    }

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
            signingConfig = signingConfigs.findByName("upload") ?: signingConfigs.getByName("debug")
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
    // Alarms and history are persisted as small JSON files (no database needed for a handful of rows).
    implementation(libs.kotlinx.serialization.json)
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
    // Video proof (details.md §10): record the workout alongside the preview. Same camerax version.
    implementation(libs.androidx.camera.video)
    // Compose viewfinder for the preview, so the workout screen stays pure Compose.
    implementation(libs.androidx.camera.compose)
    // On-device pose landmarks (CLAUDE.md stack), behind the PoseEngine interface.
    implementation(libs.mediapipe.tasks.vision)

    baselineProfile(project(":baselineprofile"))

    testImplementation(libs.junit)
}
