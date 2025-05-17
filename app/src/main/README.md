# Yoopi Player Phase 1 Patch

This archive contains new source files and resources to enable:
* Xtream / M3U login screen
* Room database for playlist caching
* Brand colors & splash gradient

## How to apply
1. Unzip into your existing `app/src/main` directory, preserving package structure.
2. Add Room & Hilt Gradle dependencies:
   ```
   implementation("androidx.room:room-runtime:2.6.1")
   kapt("androidx.room:room-compiler:2.6.1")
   implementation("androidx.room:room-ktx:2.6.1")

   implementation("com.google.dagger:hilt-android:2.51")
   kapt("com.google.dagger:hilt-compiler:2.51")
   ```
3. Sync Gradle, build, and launch.