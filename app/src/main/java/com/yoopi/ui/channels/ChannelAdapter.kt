package com.yoopi.ui.channels

import android.view.*
import android.widget.TextView
import androidx.paging.PagingDataAdapter          // ← new
import androidx.recyclerview.widget.*
import com.yoopi.data.StreamEntity
import com.yoopi.player.R

class ChannelAdapter(
    private val onClick: (StreamEntity) -> Unit
) : PagingDataAdapter<StreamEntity, ChannelAdapter.VH>(DIFF) {   // ← change

    class VH(view: View, private val click: (StreamEntity) -> Unit) :
        RecyclerView.ViewHolder(view) {

        private val title: TextView = view.findViewById(R.id.title)
        private var current: StreamEntity? = null

        init { view.setOnClickListener { current?.let(click) } }

        fun bind(item: StreamEntity?) {                         // nullable!
            current = item
            title.text = item?.name ?: "…"                      // cheap placeholder
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(
            LayoutInflater.from(p.context).inflate(R.layout.item_channel, p, false),
            onClick
        )

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<StreamEntity>() {
            override fun areItemsTheSame(a: StreamEntity, b: StreamEntity) = a.id == b.id
            override fun areContentsTheSame(a: StreamEntity, b: StreamEntity) = a == b
        }
    }
}
