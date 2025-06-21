package com.yoopi.ui.channels

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yoopi.player.R
import dagger.hilt.android.AndroidEntryPoint              // ← add

@AndroidEntryPoint
class ChannelsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The activity just hosts the fragment that already works.
        setContentView(R.layout.activity_channels)

        // Pass the id straight through to the fragment only once.
        if (savedInstanceState == null) {
            val playlistId = intent.getLongExtra("playlistId", 0L)
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.channels_container,
                    ChannelsFragment().apply {
                        arguments = Bundle().apply {
                            putLong("playlistId", playlistId)
                        }
                    }
                )
                .commit()
        }
    }
}
