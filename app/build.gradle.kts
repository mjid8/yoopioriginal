plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
    id("androidx.navigation.safeargs.kotlin")
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
    buildFeatures {
        viewBinding = true
    }


}

dependencies {
    // AndroidX base
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")

    // MaterialComponents
    implementation("com.google.android.material:material:1.9.0")
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

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
    //from the phase 2
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    implementation("com.google.dagger:hilt-android:2.51")
    kapt("com.google.dagger:hilt-compiler:2.51")
    //commit 2
    implementation("androidx.viewpager2:viewpager2:1.1.0-beta02")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.0")

    // (optional but tidy) force Lifecycle to 2.7.0 so everything is aligned
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    // manage large playlist
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Paging for huge playlists
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")

    //ipv4blanket
    implementation ("androidx.media3:media3-datasource-okhttp:1.3.1") // ⬅ new


}

