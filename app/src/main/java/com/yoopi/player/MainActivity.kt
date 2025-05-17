package com.yoopi.player

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
class MainActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var fsButton: ImageButton
    private var isFullscreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initUi()
        initPlayer()
    }

    private fun initUi() {
        playerView = findViewById(R.id.playerView)
        fsButton   = playerView.findViewById(R.id.exo_fullscreen)

        // hide controller after 3 s – like YouTube
        playerView.controllerShowTimeoutMs = 3_000
        playerView.controllerHideOnTouch   = true

        playerView.setControllerVisibilityListener(
            object : PlayerView.ControllerVisibilityListener {
                override fun onVisibilityChanged(visibility: Int) {
                    fsButton.visibility =
                        if (visibility == View.VISIBLE) View.VISIBLE else View.GONE
                }
            })

        fsButton.setOnClickListener {
            if (isFullscreen) exitFullscreen() else enterFullscreen()
        }
    }

    private fun initPlayer() {
        player = ExoPlayer.Builder(this).build().also {
            playerView.player = it
            it.setMediaItem(
                MediaItem.fromUri("https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8")
            )
            it.prepare()
            it.playWhenReady = true
        }
    }

    /* ---------- full-screen helpers ---------- */

    private fun enterFullscreen() {
        isFullscreen = true
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        fsButton.setImageResource(R.drawable.ic_fullscreen_exit)
    }

    private fun exitFullscreen() {
        isFullscreen = false
        WindowInsetsControllerCompat(window, window.decorView)
            .show(WindowInsetsCompat.Type.systemBars())
        WindowCompat.setDecorFitsSystemWindows(window, true)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        fsButton.setImageResource(R.drawable.ic_fullscreen)
    }

    /* ---------- handle rotation ---------- */

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        /* A fresh controller view was inflated – grab the button again */
        fsButton = playerView.findViewById(R.id.exo_fullscreen)

        fsButton.setOnClickListener {
            if (isFullscreen) exitFullscreen() else enterFullscreen()
        }

        playerView.setControllerVisibilityListener(
            object : PlayerView.ControllerVisibilityListener {
                override fun onVisibilityChanged(visibility: Int) {
                    fsButton.visibility =
                        if (visibility == View.VISIBLE) View.VISIBLE else View.GONE
                }
            })

        when (newConfig.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> if (!isFullscreen) enterFullscreen()
            Configuration.ORIENTATION_PORTRAIT  -> if (isFullscreen)  exitFullscreen()
        }
    }

    /* ---------- lifecycle ---------- */

    override fun onStop()    { super.onStop();    player.pause()   }
    override fun onDestroy() { super.onDestroy(); player.release() }
}
