package com.yoopi.player

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host for the Navigation graph.
 * We no longer create the NavHostFragment in code – it’s in activity_launcher.xml.
 */
@AndroidEntryPoint
class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)   // ← that’s it, nothing else
    }
}
