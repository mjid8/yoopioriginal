@file:OptIn(androidx.media3.common.util.UnstableApi::class)
package com.yoopi.player

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import com.yoopi.player.databinding.ActivityMainBinding

import androidx.media3.exoplayer.source.DefaultMediaSourceFactory   // NEW
import androidx.media3.datasource.okhttp.OkHttpDataSource
import okhttp3.OkHttpClient
import com.yoopi.player.net.Ipv4OnlyDns



/**
 * Full-screen player that expects a single stream URL via Intent extra.
 *
 *     startActivity(
 *         Intent(context, MainActivity::class.java)
 *             .putExtra(EXTRA_URL, stream.url)
 *     )
 *
 * Features retained from the legacy version:
 *  • custom full-screen button that appears with the controller
 *  • auto-rotation handling with system-bar hiding
 *  • releases ExoPlayer in onStop() to free resources
 */
class MainActivity : AppCompatActivity() {

    /* ---------- constants ---------- */
    companion object {
        const val EXTRA_URL = "url"           // Intent key
        const val EXTRA_TITLE = "title"   // ← ADD THIS LINE
    }

    /* ---------- view binding ---------- */
    private lateinit var bind:       ActivityMainBinding
    private lateinit var playerView: PlayerView
    private lateinit var fsButton:   ImageButton

    /* ---------- player ---------- */
    private lateinit var player: ExoPlayer
    private var isFullscreen = false

    /* ---------- lifecycle ---------- */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind       = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)

        playerView = bind.playerView
        fsButton   = playerView.findViewById(R.id.exo_fullscreen)

        initPlayer()
        initUi()
        loadStreamFromIntent()
    }

    override fun onStop()    { super.onStop();    player.pause()   }
    override fun onDestroy() { super.onDestroy(); player.release() }

    /* ---------- load and play ---------- */

    private fun loadStreamFromIntent() {

        // ➊ get what the fragment sent
        val url = intent.getStringExtra("url")
        if (url.isNullOrBlank()) {
            Toast.makeText(this, "No stream URL", Toast.LENGTH_SHORT).show()
            finish()                 // fall back to the list
            return
        }

        // ➋ tell ExoPlayer
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true

        // (optional) show channel name on screen
        bind.channelTitle.text = intent.getStringExtra(EXTRA_TITLE) ?: ""
    }


    /* ---------- player + UI helpers ---------- */

    private fun initPlayer() {

        /* 1️⃣  Data-source factory with a “friendly” User-Agent */

        /* 1️⃣  OkHttp client that forces IPv4 and allows redirects */
        val okClient = OkHttpClient.Builder()
            .dns(Ipv4OnlyDns())
            .followRedirects(true)
            .build()
        /* 2️⃣  Data-source factory that Media3 will actually use */

                val httpFactory = OkHttpDataSource.Factory(okClient)
                    .setUserAgent("VLC/3.0.0 Android")
        

        /* 2️⃣  Build the player – no named argument, and pass only the factory */
        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(httpFactory)     // <- just the factory
            )
            .build()
            .also { playerView.player = it }

        /* (optional) start at full volume */
        player.volume = 1f
    }



    private fun initUi() {
        playerView.controllerShowTimeoutMs = 3_000
        playerView.controllerHideOnTouch   = true

        /* show/hide custom full-screen button */
        playerView.setControllerVisibilityListener(
            object : PlayerControlView.VisibilityListener {
                override fun onVisibilityChange(visibility: Int) {
                    fsButton.visibility =
                        if (visibility == View.VISIBLE) View.VISIBLE else View.GONE
                }
            }
        )

        fsButton.setOnClickListener {
            if (isFullscreen) exitFullscreen() else enterFullscreen()
        }
    }

    /* ---------- full-screen helpers ---------- */

    private fun enterFullscreen() {
        isFullscreen = true
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat
                .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
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

    /* ---------- rotation handling ---------- */

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        /* re-wire the full-screen button after layout is recreated */
        fsButton = playerView.findViewById(R.id.exo_fullscreen)
        fsButton.setOnClickListener {
            if (isFullscreen) exitFullscreen() else enterFullscreen()
        }

        playerView.setControllerVisibilityListener(
            object : PlayerControlView.VisibilityListener {
                override fun onVisibilityChange(visibility: Int) {
                    fsButton.visibility =
                        if (visibility == View.VISIBLE) View.VISIBLE else View.GONE
                }
            }
        )

        when (newConfig.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> if (!isFullscreen) enterFullscreen()
            Configuration.ORIENTATION_PORTRAIT  -> if (isFullscreen)  exitFullscreen()
        }
    }
}
