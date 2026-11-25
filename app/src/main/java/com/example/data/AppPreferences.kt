package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.LiveChannel
import com.example.model.VodEpisode
import com.example.model.VodItem
import com.example.model.VodSeason
import com.example.model.VodType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "screen_star_prefs")

class AppPreferences(private val context: Context) {

    companion object {
        private val KEY_LANG = stringPreferencesKey("app_lang")
        private val KEY_FAVORITES = stringPreferencesKey("channels_favorites_v1")
        private val KEY_CH_STRETCH = booleanPreferencesKey("ch_stretch")
        private val KEY_VOD_STRETCH = booleanPreferencesKey("vod_stretch")

        private const val VOD_CACHE_FILE = "vod_cache_v3.json"
        private const val SERIES_CACHE_FILE = "series_info_cache_v1.json"
        private const val VOD_CACHE_TTL_MS = 30L * 60L * 1000L
        private const val SERIES_CACHE_TTL_MS = 12L * 60L * 60L * 1000L
        private const val VOD_CACHE_MAX_PER_TYPE = 500
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LANG] ?: "auto"
    }

    val favoritesFlow: Flow<Map<String, LiveChannel>> = context.dataStore.data.map { prefs ->
        parseFavoritesJson(prefs[KEY_FAVORITES])
    }

    val chStretchFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_CH_STRETCH] ?: false
    }

    val vodStretchFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_VOD_STRETCH] ?: false
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[KEY_LANG] = lang }
    }

    suspend fun saveFavorites(favorites: Map<String, LiveChannel>) {
        val root = JSONObject()
        favorites.forEach { (id, ch) ->
            val obj = JSONObject()
            obj.put("stream_id", ch.streamId)
            obj.put("name", ch.name)
            obj.put("stream_icon", ch.streamIcon)
            obj.put("category_id", ch.categoryId)
            root.put(id, obj)
        }
        context.dataStore.edit { it[KEY_FAVORITES] = root.toString() }
    }

    suspend fun setChannelStretch(stretched: Boolean) {
        context.dataStore.edit { it[KEY_CH_STRETCH] = stretched }
    }

    suspend fun setVodStretch(stretched: Boolean) {
        context.dataStore.edit { it[KEY_VOD_STRETCH] = stretched }
    }

    private fun parseFavoritesJson(raw: String?): Map<String, LiveChannel> {
        if (raw.isNullOrBlank()) return emptyMap()
        return try {
            val obj = JSONObject(raw)
            val map = linkedMapOf<String, LiveChannel>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val item = obj.optJSONObject(key) ?: continue
                map[key] = LiveChannel(
                    streamId = item.optString("stream_id", key),
                    name = item.optString("name", "---"),
                    streamIcon = item.optString("stream_icon", ""),
                    categoryId = item.optString("category_id", "")
                )
            }
            map
        } catch (_: Exception) {
            emptyMap()
        }
    }

    suspend fun readVodCache(): Pair<List<VodItem>, List<VodItem>>? = withContext(Dispatchers.IO) {
        try {
            val file = File(context.cacheDir, VOD_CACHE_FILE)
            if (!file.exists()) return@withContext null
            val obj = JSONObject(file.readText())
            val ts = obj.optLong("t", 0L)
            if (System.currentTimeMillis() - ts > VOD_CACHE_TTL_MS) return@withContext null

            val movies = parseVodArray(obj.optJSONArray("movies"), VodType.MOVIE)
            val series = parseVodArray(obj.optJSONArray("series"), VodType.SERIES)
            if (movies.isEmpty() && series.isEmpty()) null else Pair(movies, series)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun writeVodCache(movies: List<VodItem>, series: List<VodItem>) = withContext(Dispatchers.IO) {
        try {
            val obj = JSONObject()
            obj.put("t", System.currentTimeMillis())
            obj.put("movies", vodListToJson(movies.take(VOD_CACHE_MAX_PER_TYPE)))
            obj.put("series", vodListToJson(series.take(VOD_CACHE_MAX_PER_TYPE)))
            File(context.cacheDir, VOD_CACHE_FILE).writeText(obj.toString())
        } catch (_: Exception) {
        }
    }

    suspend fun clearVodCache() = withContext(Dispatchers.IO) {
        try {
            File(context.cacheDir, VOD_CACHE_FILE).delete()
        } catch (_: Exception) {
        }
    }

    suspend fun readSeriesInfoCache(seriesId: String): List<VodSeason>? = withContext(Dispatchers.IO) {
        try {
            val file = File(context.cacheDir, SERIES_CACHE_FILE)
            if (!file.exists()) return@withContext null
            val root = JSONObject(file.readText())
            val entry = root.optJSONObject(seriesId) ?: return@withContext null
            val ts = entry.optLong("t", 0L)
            if (System.currentTimeMillis() - ts > SERIES_CACHE_TTL_MS) return@withContext null
            parseSeasonsJson(entry.optJSONArray("seasons"))
        } catch (_: Exception) {
            null
        }
    }

    suspend fun writeSeriesInfoCache(seriesId: String, seasons: List<VodSeason>) = withContext(Dispatchers.IO) {
        try {
            val file = File(context.cacheDir, SERIES_CACHE_FILE)
            val root = if (file.exists()) {
                try {
                    JSONObject(file.readText())
                } catch (_: Exception) {
                    JSONObject()
                }
            } else {
                JSONObject()
            }
            val entry = JSONObject()
            entry.put("t", System.currentTimeMillis())
            val seasonsArr = JSONArray()
            seasons.forEach { season ->
                val sObj = JSONObject()
                sObj.put("seasonNumber", season.seasonNumber)
                val epsArr = JSONArray()
                season.episodes.forEach { ep ->
                    val eObj = JSONObject()
                    eObj.put("id", ep.id)
                    eObj.put("episodeNum", ep.episodeNum)
                    eObj.put("title", ep.title)
                    eObj.put("ext", ep.ext)
                    epsArr.put(eObj)
                }
                sObj.put("episodes", epsArr)
                seasonsArr.put(sObj)
            }
            entry.put("seasons", seasonsArr)
            root.put(seriesId, entry)

            // Prune to max 100 entries
            if (root.length() > 100) {
                val list = mutableListOf<Pair<String, Long>>()
                val keys = root.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val t = root.optJSONObject(k)?.optLong("t", 0L) ?: 0L
                    list.add(k to t)
                }
                list.sortedByDescending { it.second }.drop(100).forEach { root.remove(it.first) }
            }
            file.writeText(root.toString())
        } catch (_: Exception) {
        }
    }

    private fun parseVodArray(arr: JSONArray?, type: VodType): List<VodItem> {
        if (arr == null) return emptyList()
        val list = ArrayList<VodItem>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            list.add(
                VodItem(
                    type = type,
                    id = o.optString("id"),
                    title = o.optString("title", "---"),
                    poster = o.optString("poster", ""),
                    rating = o.optString("rating", ""),
                    ext = o.optString("ext", "mp4")
                )
            )
        }
        return list
    }

    private fun vodListToJson(items: List<VodItem>): JSONArray {
        val arr = JSONArray()
        items.forEach { item ->
            val o = JSONObject()
            o.put("id", item.id)
            o.put("title", item.title)
            o.put("poster", item.poster)
            o.put("rating", item.rating)
            o.put("ext", item.ext)
            arr.put(o)
        }
        return arr
    }

    private fun parseSeasonsJson(arr: JSONArray?): List<VodSeason>? {
        if (arr == null) return null
        val seasons = mutableListOf<VodSeason>()
        for (i in 0 until arr.length()) {
            val sObj = arr.optJSONObject(i) ?: continue
            val sNum = sObj.optInt("seasonNumber", 1)
            val epsArr = sObj.optJSONArray("episodes") ?: continue
            val eps = mutableListOf<VodEpisode>()
            for (j in 0 until epsArr.length()) {
                val eObj = epsArr.optJSONObject(j) ?: continue
                eps.add(
                    VodEpisode(
                        id = eObj.optString("id"),
                        episodeNum = eObj.optString("episodeNum", ""),
                        title = eObj.optString("title", ""),
                        ext = eObj.optString("ext", "mp4")
                    )
                )
            }
            seasons.add(VodSeason(sNum, eps))
        }
        return seasons
    }
}
