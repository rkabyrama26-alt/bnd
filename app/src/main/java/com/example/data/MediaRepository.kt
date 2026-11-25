package com.example.data

import com.example.localization.AppStrings
import com.example.model.LiveCategory
import com.example.model.LiveChannel
import com.example.model.MatchEvent
import com.example.model.MatchState
import com.example.model.NewsArticle
import com.example.model.VodEpisode
import com.example.model.VodItem
import com.example.model.VodSeason
import com.example.model.VodType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object MediaConfig {
    const val HOST = "http://305bo.xgek.codes"
    const val USERNAME = "99483920294"
    const val PASSWORD = "9439488449"
    const val MAX_CHANNELS = 700
    const val MAX_VOD_ITEMS = 700
    const val DEFAULT_TEAM_LOGO = "https://a.espncdn.com/i/teamlogos/soccer/500/default-team-logo-500.png"
    const val SCHEDULE_API = "https://site.api.espn.com/apis/site/v2/sports/soccer/all/scoreboard"
    const val NEWS_API = "https://site.api.espn.com/apis/site/v2/sports/soccer/all/news?limit=30"

    fun liveStreamUrl(streamId: String): String =
        "$HOST/live/${encode(USERNAME)}/${encode(PASSWORD)}/$streamId.m3u8"

    fun vodMovieUrl(movieId: String, ext: String): String {
        val cleanExt = ext.ifBlank { "mp4" }
        return "$HOST/movie/${encode(USERNAME)}/${encode(PASSWORD)}/$movieId.$cleanExt"
    }

    fun vodEpisodeUrl(episodeId: String, ext: String): String {
        val cleanExt = ext.ifBlank { "mp4" }
        return "$HOST/series/${encode(USERNAME)}/${encode(PASSWORD)}/$episodeId.$cleanExt"
    }

    fun playerApiBase(): String =
        "$HOST/player_api.php?username=${encode(USERNAME)}&password=${encode(PASSWORD)}"

    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")
}

class MediaRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(40, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    private suspend fun fetchString(url: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Android) ScreenStar/1.0")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("HTTP ${response.code}")
            }
            response.body?.string() ?: throw IllegalStateException("Empty response")
        }
    }

    suspend fun fetchLiveCategories(): List<LiveCategory> {
        val raw = fetchString("${MediaConfig.playerApiBase()}&action=get_live_categories")
        val arr = JSONArray(raw)
        val result = ArrayList<LiveCategory>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val id = o.optString("category_id", "").trim()
            val name = o.optString("category_name", "---")
            if (id.isNotEmpty()) {
                result.add(LiveCategory(categoryId = id, categoryName = name))
            }
        }
        return result
    }

    suspend fun fetchLiveStreams(categoryId: String? = null): List<LiveChannel> {
        val url = if (categoryId.isNullOrBlank()) {
            "${MediaConfig.playerApiBase()}&action=get_live_streams"
        } else {
            "${MediaConfig.playerApiBase()}&action=get_live_streams&category_id=${URLEncoder.encode(categoryId, "UTF-8")}"
        }
        val raw = fetchString(url)
        val arr = JSONArray(raw)
        val limit = if (categoryId == null) minOf(arr.length(), MediaConfig.MAX_CHANNELS) else arr.length()
        val result = ArrayList<LiveChannel>(limit)
        for (i in 0 until limit) {
            val o = arr.optJSONObject(i) ?: continue
            val streamId = o.opt("stream_id")?.toString() ?: continue
            result.add(
                LiveChannel(
                    streamId = streamId,
                    name = o.optString("name", "---"),
                    streamIcon = o.optString("stream_icon", ""),
                    categoryId = o.opt("category_id")?.toString()?.trim() ?: ""
                )
            )
        }
        return result
    }

    suspend fun fetchVodMovies(): List<VodItem> {
        val raw = fetchString("${MediaConfig.playerApiBase()}&action=get_vod_streams")
        val arr = JSONArray(raw)
        val result = ArrayList<VodItem>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val id = o.opt("stream_id")?.toString() ?: continue
            result.add(
                VodItem(
                    type = VodType.MOVIE,
                    id = id,
                    title = o.optString("name", "---"),
                    poster = o.optString("stream_icon", ""),
                    rating = o.opt("rating")?.toString() ?: "",
                    ext = o.optString("container_extension", "mp4").ifBlank { "mp4" }
                )
            )
        }
        return result
    }

    suspend fun fetchVodSeries(): List<VodItem> {
        val raw = fetchString("${MediaConfig.playerApiBase()}&action=get_series")
        val arr = JSONArray(raw)
        val result = ArrayList<VodItem>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val id = o.opt("series_id")?.toString() ?: continue
            result.add(
                VodItem(
                    type = VodType.SERIES,
                    id = id,
                    title = o.optString("name", "---"),
                    poster = o.optString("cover", ""),
                    rating = o.opt("rating")?.toString() ?: "",
                    ext = "mp4"
                )
            )
        }
        return result
    }

    suspend fun fetchSeriesInfo(seriesId: String): List<VodSeason> {
        val raw = fetchString(
            "${MediaConfig.playerApiBase()}&action=get_series_info&series_id=${URLEncoder.encode(seriesId, "UTF-8")}"
        )
        val root = JSONObject(raw)
        val episodesObj = root.optJSONObject("episodes") ?: return emptyList()
        val seasonNums = mutableListOf<Int>()
        val keys = episodesObj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            key.toIntOrNull()?.let { seasonNums.add(it) }
        }
        seasonNums.sort()

        val seasons = mutableListOf<VodSeason>()
        for (seasonNum in seasonNums) {
            val arr = episodesObj.optJSONArray(seasonNum.toString()) ?: continue
            if (arr.length() == 0) continue
            val eps = mutableListOf<VodEpisode>()
            for (i in 0 until arr.length()) {
                val ep = arr.optJSONObject(i) ?: continue
                val epId = ep.opt("id")?.toString() ?: continue
                val epNum = ep.opt("episode_num")?.toString() ?: ""
                val title = ep.optString("title", "").ifBlank { epNum }
                val ext = ep.optString("container_extension", "mp4").ifBlank { "mp4" }
                eps.add(VodEpisode(id = epId, episodeNum = epNum, title = title, ext = ext))
            }
            if (eps.isNotEmpty()) {
                seasons.add(VodSeason(seasonNumber = seasonNum, episodes = eps))
            }
        }
        return seasons
    }

    suspend fun fetchSchedule(): List<MatchEvent> {
        val raw = fetchString(MediaConfig.SCHEDULE_API)
        val root = JSONObject(raw)
        val events = root.optJSONArray("events") ?: return emptyList()
        val result = mutableListOf<MatchEvent>()
        for (i in 0 until events.length()) {
            val ev = events.optJSONObject(i) ?: continue
            val comp = ev.optJSONArray("competitions")?.optJSONObject(0) ?: continue
            val competitors = comp.optJSONArray("competitors") ?: continue
            if (competitors.length() < 2) continue

            var home: JSONObject? = null
            var away: JSONObject? = null
            for (j in 0 until competitors.length()) {
                val c = competitors.optJSONObject(j) ?: continue
                when (c.optString("homeAway")) {
                    "home" -> if (home == null) home = c
                    "away" -> if (away == null) away = c
                }
            }
            val finalHome = home ?: competitors.optJSONObject(0) ?: continue
            val finalAway = away ?: competitors.optJSONObject(1) ?: continue

            val homeTeam = finalHome.optJSONObject("team") ?: JSONObject()
            val awayTeam = finalAway.optJSONObject("team") ?: JSONObject()

            val homeName = AppStrings.getArabicTeamName(
                homeTeam.optString("displayName").ifBlank { homeTeam.optString("shortDisplayName") }
            )
            val awayName = AppStrings.getArabicTeamName(
                awayTeam.optString("displayName").ifBlank { awayTeam.optString("shortDisplayName") }
            )

            val homeLogo = homeTeam.optString("logo").ifBlank { MediaConfig.DEFAULT_TEAM_LOGO }
            val awayLogo = awayTeam.optString("logo").ifBlank { MediaConfig.DEFAULT_TEAM_LOGO }

            val statusState = ev.optJSONObject("status")
                ?.optJSONObject("type")
                ?.optString("state", "pre") ?: "pre"

            val matchState = when (statusState) {
                "in" -> MatchState.LIVE
                "post" -> MatchState.FINAL
                else -> MatchState.SCHEDULED
            }

            result.add(
                MatchEvent(
                    id = ev.optString("id", i.toString()),
                    homeName = homeName,
                    awayName = awayName,
                    homeLogo = homeLogo,
                    awayLogo = awayLogo,
                    homeScore = finalHome.optString("score", "-").ifBlank { "-" },
                    awayScore = finalAway.optString("score", "-").ifBlank { "-" },
                    homeWinner = finalHome.optBoolean("winner", false),
                    awayWinner = finalAway.optBoolean("winner", false),
                    state = matchState,
                    dateIso = ev.optString("date", "")
                )
            )
        }
        return result
    }

    suspend fun fetchNews(): List<NewsArticle> = coroutineScope {
        val raw = fetchString(MediaConfig.NEWS_API)
        val root = JSONObject(raw)
        val articles = root.optJSONArray("articles") ?: return@coroutineScope emptyList()

        val deferredArticles = (0 until articles.length()).mapNotNull { idx ->
            val art = articles.optJSONObject(idx) ?: return@mapNotNull null
            async(Dispatchers.IO) {
                val rawTitle = art.optString("headline", "No Title")
                val rawDesc = art.optString("description", "")
                val image = art.optJSONArray("images")?.optJSONObject(0)?.optString("url", "") ?: ""
                val published = art.optString("published", "")

                val translatedTitleDeferred = async { translateText(rawTitle) }
                val translatedDescDeferred = async {
                    if (rawDesc.isNotBlank()) translateText(rawDesc) else ""
                }

                NewsArticle(
                    title = translatedTitleDeferred.await(),
                    description = translatedDescDeferred.await(),
                    imageUrl = image,
                    publishedIso = published
                )
            }
        }
        deferredArticles.awaitAll()
    }

    private suspend fun translateText(text: String): String {
        if (text.isBlank()) return ""
        return try {
            val encoded = URLEncoder.encode(text, "UTF-8")
            val url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=ar&dt=t&q=$encoded"
            val raw = fetchString(url)
            val arr = JSONArray(raw)
            val sentences = arr.optJSONArray(0) ?: return text
            val sb = StringBuilder()
            for (i in 0 until sentences.length()) {
                val part = sentences.optJSONArray(i)?.optString(0, "") ?: ""
                sb.append(part)
            }
            sb.toString().ifBlank { text }
        } catch (_: Exception) {
            text
        }
    }
}
