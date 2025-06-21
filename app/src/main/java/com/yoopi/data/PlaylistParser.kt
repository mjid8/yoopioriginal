package com.yoopi.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.BufferedSource
import org.json.JSONArray
import java.util.concurrent.TimeUnit
import java.util.HashSet

/* ─── HTTP helper ─── */
private val http = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(0,  TimeUnit.SECONDS)      // no per-request limit
    .build()

private const val TAG        = "PlaylistParser"
private const val BATCH_SIZE = 500          // smaller batch → lower RAM

/* ───────────────────── helpers, shared by both parsers ───────────────────── */
private fun classify(raw: String): String = when {
    raw.contains("movie",  true) ||
            raw.contains("vod",    true) -> "Movie"

    raw.contains("series", true) ||
            raw.contains("show",   true) -> "Series"

    else                         -> "Live"
}

private val rgxGroupTitle = Regex("""group-title="([^"]*)"""", RegexOption.IGNORE_CASE)

/* ─────────────────────────────── object ─────────────────────────────── */
object PlaylistParser {

    /**
     * Streams every channel/VOD from the given playlist into the database.
     * Memory stays low because rows are flushed in batches.
     */
    suspend fun parse(
        entity: PlaylistEntity,
        dao:    PlaylistDao
    ) = withContext(Dispatchers.IO) {

        val buffer = ArrayList<StreamEntity>(BATCH_SIZE)
        val seen   = HashSet<String>(BATCH_SIZE)

        suspend fun flush() {
            if (buffer.isNotEmpty()) {
                dao.insertStreams(buffer.toList())
                Log.d(TAG, "✓ Flushed ${buffer.size} rows (total so far)")
                buffer.clear()
                seen.clear()            // keep HashSet tiny
            }
        }

        when (entity.type) {
            PlaylistType.M3U    -> m3u(entity, entity.url, buffer, seen, ::flush)
            PlaylistType.XTREAM -> {
                // (1) try category sync
                var ok = xtreamSync(entity, buffer, seen, ::flush)

                // (2) if blocked, fall back to paged player_api
                if (!ok) {
                    ok = fetchPaged(entity, "get_live_streams",
                        buffer, seen, ::flush)
                    ok = fetchPaged(entity, "get_vod_streams",
                        buffer, seen, ::flush) || ok
                }

                // (3) absolute last chance – global M3U
                if (!ok) {
                    val m3u = "${entity.url}/get.php?username=${entity.username}" +
                            "&password=${entity.password}&type=m3u_plus&output=ts"
                    m3u(entity, m3u, buffer, seen, ::flush)
                }
            }
        }
        flush()                // write any tail rows
    }

    /* ─────────── plain M3U handling ─────────── */

    private suspend fun m3u(
        entity: PlaylistEntity,
        link:   String,
        buffer: MutableList<StreamEntity>,
        seen:   HashSet<String>,
        flush:  suspend () -> Unit
    ): Boolean {

        Log.d(TAG, "⇢ Streaming $link")
        val rsp = http.newCall(
            Request.Builder()
                .url(link)
                .header("User-Agent", "TiviMate/5.0")
                .build()
        ).execute()

        if (rsp.isSuccessful) {
            rsp.body!!.source().use { src ->
                parseM3uStream(entity, src, buffer, seen, flush)
            }
            return buffer.isNotEmpty()
        }

        // fallback: live-only JSON without paging
        val jsonUrl = link.replace("get.php?",  "player_api.php?")
            .replace("&type=m3u_plus&output=ts", "")
            .plus("&action=get_live_streams")

        return json(entity, jsonUrl, buffer, seen, flush)
    }

    private suspend fun parseM3uStream(
        entity: PlaylistEntity,
        source: BufferedSource,
        buffer: MutableList<StreamEntity>,
        seen:   HashSet<String>,
        flush:  suspend () -> Unit
    ) {
        var pendingName : String? = null
        var pendingGroup: String  = "Live"           // default

        while (!source.exhausted()) {
            val line = source.readUtf8Line()?.trim().orEmpty()
            if (line.isEmpty()) continue

            when {
                line.startsWith("#EXTINF", true) -> {
                    pendingName  = line.substringAfter(',').ifBlank {
                        "Channel ${buffer.size + 1}"
                    }
                    val g = rgxGroupTitle.find(line)?.groupValues?.get(1).orEmpty()
                    pendingGroup = classify(g)
                }

                pendingName != null && line.startsWith("http", true) -> {
                    if (seen.add(line)) {
                        buffer += StreamEntity(
                            playlistId = entity.id,
                            name       = pendingName,
                            url        = line,
                            group      = pendingGroup
                        )
                        if (buffer.size >= BATCH_SIZE) flush()
                    }
                    pendingName = null
                }
            }
        }
    }

    /* ─────────── Xtream full category sync ─────────── */

    private suspend fun xtreamSync(
        entity: PlaylistEntity,
        buffer: MutableList<StreamEntity>,
        seen:   HashSet<String>,
        flush:  suspend () -> Unit
    ): Boolean {

        val base  = entity.url.trimEnd('/')
        val creds = "username=${entity.username}&password=${entity.password}"

        val liveCats = "$base/player_api.php?$creds&action=get_live_categories"
        val vodCats  = "$base/player_api.php?$creds&action=get_vod_categories"

        val liveOk = loopCategories(entity, liveCats, "get_live_streams",
            buffer, seen, flush)
        val vodOk  = loopCategories(entity, vodCats,  "get_vod_streams",
            buffer, seen, flush)

        return liveOk || vodOk
    }

    private suspend fun loopCategories(
        entity:        PlaylistEntity,
        catsUrl:       String,
        streamsAction: String,
        buffer:        MutableList<StreamEntity>,
        seen:          HashSet<String>,
        flush:         suspend () -> Unit
    ): Boolean {

        val rsp = http.newCall(Request.Builder().url(catsUrl).build()).execute()
        if (!rsp.isSuccessful) {
            Log.w(TAG, "Categories call failed: HTTP ${rsp.code}")
            return false
        }

        val arr = JSONArray(rsp.body!!.string())
        if (arr.length() == 0) return false

        val base  = entity.url.trimEnd('/')
        val creds = "username=${entity.username}&password=${entity.password}"

        for (i in 0 until arr.length()) {
            val id = arr.getJSONObject(i).optString("category_id", "")
            if (id.isBlank()) continue

            val url = "$base/player_api.php?$creds&action=$streamsAction" +
                    "&category_id=$id"
            json(entity, url, buffer, seen, flush)   // ignore per-cat errors
        }
        return buffer.isNotEmpty()
    }

    /* ─────────── pagination helper ─────────── */

    private suspend fun fetchPaged(
        entity: PlaylistEntity,
        action: String,                  // get_live_streams | get_vod_streams
        buffer: MutableList<StreamEntity>,
        seen:   HashSet<String>,
        flush:  suspend () -> Unit
    ): Boolean {

        val base  = entity.url.trimEnd('/')
        var page  = 1
        val limit = 5_000
        var any   = false

        while (true) {
            val url = "$base/player_api.php?username=${entity.username}" +
                    "&password=${entity.password}&action=$action" +
                    "&page=$page&limit=$limit"

            val added = json(entity, url, buffer, seen, flush)
            if (!added) break            // no more pages

            any  = true
            page += 1
        }
        return any
    }

    /* ─────────── generic player_api parser ─────────── */

    private suspend fun json(
        entity: PlaylistEntity,
        link:   String,
        buffer: MutableList<StreamEntity>,
        seen:   HashSet<String>,
        flush:  suspend () -> Unit
    ): Boolean {

        val rsp = http.newCall(Request.Builder().url(link).build()).execute()
        if (!rsp.isSuccessful) {
            Log.w(TAG, "player_api failed (${rsp.code}) for $link")
            return false
        }

        val body = rsp.body!!.string()

        val channelsJson = when {
            body.startsWith("[") -> body
            else -> Regex("\"(?:available_)?channels\"\\s*:\\s*(\\[.*])")
                .find(body)?.groupValues?.get(1).orEmpty()
        }
        if (channelsJson.isBlank()) return false

        val arr = JSONArray(channelsJson)
        for (i in 0 until arr.length()) {

            val o         = arr.getJSONObject(i)
            val name      = o.optString("name",
                "Channel ${buffer.size + 1}")

            var url       = o.optString("stream_url", "")
            if (url.isBlank()) {                    // build it ourselves
                val id  = o.optInt("stream_id", -1)
                val ext = o.optString("container_extension", "ts")
                if (id != -1) {
                    val base = entity.url.trimEnd('/')
                    url = "$base/live/${entity.username}" +
                            "/${entity.password}/$id.$ext"
                }
            }

            // derive Live / Movie / Series from API fields
            val rawGroup  = o.optString("stream_type",
                o.optString("category_name", ""))
            val group     = classify(rawGroup)

            if (url.isNotBlank() && seen.add(url)) {
                buffer += StreamEntity(
                    playlistId = entity.id,
                    name       = name,
                    url        = url,
                    group      = group
                )
                if (buffer.size >= BATCH_SIZE) flush()
            }
        }
        return arr.length() > 0
    }
}
