package com.yoopi.ui.channels

/** Simple container for the three totals shown in the header row */
data class PlaylistStats(
    val live   : Int = 0,
    val movies : Int = 0,
    val series : Int = 0
)
