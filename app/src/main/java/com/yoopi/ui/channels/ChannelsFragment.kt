package com.yoopi.ui.channels

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView                // ← NEW
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yoopi.data.StreamEntity
import com.yoopi.player.MainActivity
import com.yoopi.player.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChannelsFragment : Fragment() {

    private val vm: ChannelsViewModel by viewModels()
    private val adapter by lazy { ChannelAdapter(::openPlayer) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_channels, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        /* ---------- views ---------- */
        val recycler   = view.findViewById<RecyclerView>(R.id.recycler)
        val liveTxt    = view.findViewById<TextView>(R.id.liveCount)
        val movieTxt   = view.findViewById<TextView>(R.id.movieCount)
        val seriesTxt  = view.findViewById<TextView>(R.id.seriesCount)

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter       = adapter

        /* ---------- ①  paging  ---------- */
        viewLifecycleOwner.lifecycleScope.launch {
            vm.pagingFlow.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        /* ---------- ②  counters  ---------- */
        viewLifecycleOwner.lifecycleScope.launch {
            vm.statsFlow.collectLatest { stats ->
                liveTxt.text   = "Channels: ${stats.live}"
                movieTxt.text  = "Movies:   ${stats.movies}"
                seriesTxt.text = "Series:   ${stats.series}"
            }
        }
    }

    private fun openPlayer(stream: StreamEntity) {
        startActivity(
            Intent(requireContext(), MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_URL,   stream.url)
                putExtra(MainActivity.EXTRA_TITLE, stream.name)
            }
        )
    }
}
