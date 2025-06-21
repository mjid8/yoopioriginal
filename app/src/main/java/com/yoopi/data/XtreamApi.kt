package com.yoopi.data

import android.util.Log
import androidx.media3.common.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.concurrent.TimeUnit

/** ---------------- networking helper ---------------- */
private val http = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

private const val TAG = "XtreamApi"

/* ───────────────────────────────────────────────────── */
object XtreamApi {

    /** 1. categories ────────────────────────────────── */
    suspend fun liveCategories(base: String, user: String, pass: String)
            : List<Category> = withContext(Dispatchers.IO) {

        val url = "$base/player_api.php?username=$user&password=$pass&action=get_live_categories"
        val json = get(url) ?: return@withContext emptyList()

        val out = mutableListOf<Category>()
        val arr = JSONArray(json)
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out += Category(
                id   = o.optString("category_id"),
                name = o.optString("category_name", "Unnamed")
            )
        }
        out
    }

    /** 2. streams in a category ─────────────────────── */
    suspend fun liveStreams(
        base: String,
        user: String,
        pass: String,
        categoryId: String
    ): List<MediaItem> = withContext(Dispatchers.IO) {

        val url = "$base/player_api.php?username=$user&password=$pass" +
                "&action=get_live_streams&category_id=$categoryId"
        val json = get(url) ?: return@withContext emptyList()

        val out = mutableListOf<MediaItem>()
        val arr = JSONArray(json)
        for (i in 0 until arr.length()) {
            val o   = arr.getJSONObject(i)
            val url = o.optString("stream_url")
            if (url.isBlank()) continue

            out += MediaItem.Builder()
                .setUri(url)
                .setMediaId(url)
                .setTag(o.optString("name"))
                .build()
        }
        out
    }

    /* ---------------- helpers ------------------------ */

    private fun get(url: String): String? {
        Log.d(TAG, "⇢ $url")
        val rsp = http.newCall(Request.Builder().url(url).build()).execute()
        if (!rsp.isSuccessful) {
            Log.e(TAG, "HTTP ${rsp.code} for $url")
            return null
        }
        return rsp.body?.string()
    }

    data class Category(val id: String, val name: String)
}
