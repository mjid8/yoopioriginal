plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.yoopi.player"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yoopi.player"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "0.1-alpha"
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    // Remove density split (obsolete)
    splits {
        abi { isEnable = false }
        // density split removed (no longer needed)
    }

    bundle {
        language { enableSplit = false }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // AndroidX base
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")

    // MaterialComponents
    implementation("com.google.android.material:material:1.9.0")

    // Media3 (current stable version)
    // Media3 (MUST be same version for all components)
    val media3 = "1.3.1"  // ← Confirm this is identical in all media3 entries
    implementation("androidx.media3:media3-exoplayer:$media3")
    implementation("androidx.media3:media3-ui:$media3")  // ← This provides the layout
    implementation("androidx.media3:media3-exoplayer-hls:$media3")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Image loading
    implementation("io.coil-kt:coil:2.6.0")
}