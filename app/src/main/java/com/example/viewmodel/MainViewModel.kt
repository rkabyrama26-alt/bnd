package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppPreferences
import com.example.data.MediaConfig
import com.example.data.MediaRepository
import com.example.localization.AppStrings
import com.example.model.AppScreen
import com.example.model.LiveCategory
import com.example.model.LiveChannel
import com.example.model.MatchEvent
import com.example.model.NewsArticle
import com.example.model.VodEpisode
import com.example.model.VodItem
import com.example.model.VodSeason
import com.example.model.VodType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

data class MainUiState(
    val currentScreen: AppScreen = AppScreen.HOME,
    val userLangSetting: String = "auto",
    val activeLang: String = "ar",
    val isLanguageModalOpen: Boolean = false,
    val isSupportModalOpen: Boolean = false,

    // Channels
    val categories: List<LiveCategory> = emptyList(),
    val allChannels: List<LiveChannel> = emptyList(),
    val categoryCache: Map<String, List<LiveChannel>> = emptyMap(),
    val currentCategory: String = "all",
    val activeChannel: LiveChannel? = null,
    val channelSearchQuery: String = "",
    val channelsLoading: Boolean = false,
    val channelsCategoryLoading: Boolean = false,
    val channelsError: String? = null,
    val favoriteChannels: Map<String, LiveChannel> = emptyMap(),
    val chStretch: Boolean = false,

    // Schedule
    val matches: List<MatchEvent> = emptyList(),
    val scheduleLoading: Boolean = false,
    val scheduleError: String? = null,

    // News
    val news: List<NewsArticle> = emptyList(),
    val newsLoading: Boolean = false,
    val newsError: String? = null,

    // VOD
    val vodFilter: VodType = VodType.SERIES,
    val vodItems: List<VodItem> = emptyList(),
    val vodLoading: Boolean = false,
    val vodError: Boolean? = null,
    val vodSearchQuery: String = "",

    // Episodes
    val selectedSeries: VodItem? = null,
    val episodesBySeason: List<VodSeason> = emptyList(),
    val episodesLoading: Boolean = false,
    val episodesError: String? = null,

    // VOD Player
    val vodPlayerUrl: String? = null,
    val vodPlayerTitle: String = "",
    val vodPlayerSub: String = "",
    val vodStretch: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = AppPreferences(application)
    private val repo = MediaRepository()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var channelsLoaded = false
    private var scheduleLoaded = false
    private var newsLoaded = false
    private var vodLoaded = false
    private var seriesPreloadStarted = false

    private var vodMovies: List<VodItem> = emptyList()
    private var vodSeries: List<VodItem> = emptyList()
    private val seriesInfoMemoryCache = mutableMapOf<String, List<VodSeason>>()

    init {
        viewModelScope.launch {
            prefs.languageFlow.collect { setting ->
                _uiState.update {
                    it.copy(
                        userLangSetting = setting,
                        activeLang = AppStrings.detectLanguage(setting)
                    )
                }
            }
        }
        viewModelScope.launch {
            prefs.favoritesFlow.collect { favs ->
                _uiState.update { it.copy(favoriteChannels = favs) }
            }
        }
        viewModelScope.launch {
            prefs.chStretchFlow.collect { stretched ->
                _uiState.update { it.copy(chStretch = stretched) }
            }
        }
        viewModelScope.launch {
            prefs.vodStretchFlow.collect { stretched ->
                _uiState.update { it.copy(vodStretch = stretched) }
            }
        }

        // Preload channels 500ms after app launch just like HTML
        viewModelScope.launch {
            delay(500)
            loadChannels()
        }
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
        when (screen) {
            AppScreen.CHANNELS -> {
                if (!channelsLoaded && !_uiState.value.channelsLoading) {
                    loadChannels()
                } else if (channelsLoaded && _uiState.value.activeChannel == null && _uiState.value.allChannels.isNotEmpty()) {
                    _uiState.update { it.copy(activeChannel = it.allChannels.first()) }
                }
            }
            AppScreen.SCHEDULE -> if (!scheduleLoaded) loadSchedule()
            AppScreen.NEWS -> if (!newsLoaded) loadNews()
            AppScreen.VOD -> if (!vodLoaded && !_uiState.value.vodLoading) loadVod()
            else -> Unit
        }
    }

    fun navigateBack() {
        val current = _uiState.value.currentScreen
        when (current) {
            AppScreen.VOD_PLAYER -> {
                val returnTo = if (_uiState.value.selectedSeries != null) AppScreen.EPISODES else AppScreen.VOD
                _uiState.update {
                    it.copy(
                        currentScreen = returnTo,
                        vodPlayerUrl = null,
                        vodPlayerTitle = "",
                        vodPlayerSub = ""
                    )
                }
            }
            AppScreen.EPISODES -> {
                _uiState.update { it.copy(currentScreen = AppScreen.VOD, selectedSeries = null) }
            }
            AppScreen.CHANNELS -> {
                _uiState.update { it.copy(currentScreen = AppScreen.HOME, activeChannel = null) }
            }
            AppScreen.SETTINGS -> {
                if (_uiState.value.isLanguageModalOpen || _uiState.value.isSupportModalOpen) {
                    _uiState.update { it.copy(isLanguageModalOpen = false, isSupportModalOpen = false) }
                } else {
                    _uiState.update { it.copy(currentScreen = AppScreen.HOME) }
                }
            }
            else -> _uiState.update { it.copy(currentScreen = AppScreen.HOME) }
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            prefs.setLanguage(lang)
            _uiState.update { it.copy(isLanguageModalOpen = false) }
        }
    }

    fun setLanguageModalOpen(open: Boolean) {
        _uiState.update { it.copy(isLanguageModalOpen = open) }
    }

    fun setSupportModalOpen(open: Boolean) {
        _uiState.update { it.copy(isSupportModalOpen = open) }
    }

    fun toggleChannelStretch() {
        val next = !_uiState.value.chStretch
        _uiState.update { it.copy(chStretch = next) }
        viewModelScope.launch { prefs.setChannelStretch(next) }
    }

    fun toggleVodStretch() {
        val next = !_uiState.value.vodStretch
        _uiState.update { it.copy(vodStretch = next) }
        viewModelScope.launch { prefs.setVodStretch(next) }
    }

    // Channels
    fun loadChannels() {
        if (channelsLoaded || _uiState.value.channelsLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(channelsLoading = true, channelsError = null) }
            try {
                val catsDeferred = async { repo.fetchLiveCategories() }
                val chsDeferred = async { repo.fetchLiveStreams(null) }
                val cats = catsDeferred.await()
                val chs = chsDeferred.await()
                channelsLoaded = true
                _uiState.update { state ->
                    val autoPlay = if (state.currentScreen == AppScreen.CHANNELS && state.activeChannel == null) {
                        chs.firstOrNull()
                    } else {
                        state.activeChannel
                    }
                    state.copy(
                        categories = cats,
                        allChannels = chs,
                        channelsLoading = false,
                        activeChannel = autoPlay
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        channelsLoading = false,
                        channelsError = e.message ?: "Connection failed"
                    )
                }
            }
        }
    }

    fun selectCategory(categoryId: String) {
        _uiState.update { it.copy(currentCategory = categoryId, channelsError = null) }
        if (categoryId == "__favorites__" || categoryId == "all") return

        val state = _uiState.value
        if (state.categoryCache.containsKey(categoryId)) return

        val localFiltered = state.allChannels.filter { it.categoryId == categoryId.trim() }
        if (localFiltered.isNotEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(channelsCategoryLoading = true) }
            try {
                val list = repo.fetchLiveStreams(categoryId)
                _uiState.update {
                    it.copy(
                        categoryCache = it.categoryCache + (categoryId to list),
                        channelsCategoryLoading = false
                    )
                }
            } catch (e: Exception) {
                if (_uiState.value.currentCategory == categoryId) {
                    _uiState.update {
                        it.copy(
                            channelsCategoryLoading = false,
                            channelsError = e.message ?: "Error"
                        )
                    }
                }
            }
        }
    }

    fun updateChannelSearchQuery(query: String) {
        _uiState.update { it.copy(channelSearchQuery = query) }
    }

    fun playChannel(channel: LiveChannel) {
        _uiState.update { it.copy(activeChannel = channel) }
    }

    fun toggleFavorite(channel: LiveChannel) {
        val currentFavs = _uiState.value.favoriteChannels.toMutableMap()
        if (currentFavs.containsKey(channel.streamId)) {
            currentFavs.remove(channel.streamId)
        } else {
            currentFavs[channel.streamId] = channel
        }
        _uiState.update { it.copy(favoriteChannels = currentFavs) }
        viewModelScope.launch {
            prefs.saveFavorites(currentFavs)
        }
    }

    // Schedule
    fun loadSchedule() {
        if (_uiState.value.scheduleLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(scheduleLoading = true, scheduleError = null) }
            try {
                val events = repo.fetchSchedule()
                scheduleLoaded = true
                _uiState.update { it.copy(matches = events, scheduleLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(scheduleLoading = false, scheduleError = e.message ?: "Failed")
                }
            }
        }
    }

    // News
    fun loadNews() {
        if (_uiState.value.newsLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(newsLoading = true, newsError = null) }
            try {
                val articles = repo.fetchNews()
                newsLoaded = true
                _uiState.update { it.copy(news = articles, newsLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(newsLoading = false, newsError = e.message ?: "Failed")
                }
            }
        }
    }

    // VOD
    fun setVodFilter(type: VodType) {
        _uiState.update { it.copy(vodFilter = type) }
    }

    fun updateVodSearchQuery(query: String) {
        _uiState.update { it.copy(vodSearchQuery = query) }
    }

    fun loadVod() {
        if (_uiState.value.vodLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(vodLoading = true, vodError = null) }
            val cached = prefs.readVodCache()
            if (cached != null && (cached.first.isNotEmpty() || cached.second.isNotEmpty())) {
                vodMovies = cached.first
                vodSeries = cached.second
                vodLoaded = true
                finalizeVod()
                return@launch
            }

            val moviesDeferred = async { runCatching { repo.fetchVodMovies() }.getOrDefault(emptyList()) }
            val seriesDeferred = async { runCatching { repo.fetchVodSeries() }.getOrDefault(emptyList()) }

            vodMovies = moviesDeferred.await()
            vodSeries = seriesDeferred.await()
            vodLoaded = true

            if (vodMovies.isNotEmpty() || vodSeries.isNotEmpty()) {
                prefs.writeVodCache(vodMovies, vodSeries)
            }
            finalizeVod()
        }
    }

    fun reloadVod() {
        vodLoaded = false
        vodMovies = emptyList()
        vodSeries = emptyList()
        seriesPreloadStarted = false
        viewModelScope.launch {
            prefs.clearVodCache()
            loadVod()
        }
    }

    private fun finalizeVod() {
        if (vodMovies.isEmpty() && vodSeries.isEmpty()) {
            _uiState.update { it.copy(vodItems = emptyList(), vodLoading = false, vodError = true) }
            return
        }
        val combined = interleaveVod(vodMovies, vodSeries).take(MediaConfig.MAX_VOD_ITEMS)
        _uiState.update { it.copy(vodItems = combined, vodLoading = false, vodError = null) }
        preloadTopSeries()
    }

    private fun interleaveVod(movies: List<VodItem>, series: List<VodItem>): List<VodItem> {
        val out = ArrayList<VodItem>(movies.size + series.size)
        val max = maxOf(movies.size, series.size)
        for (i in 0 until max) {
            if (i < movies.size) out.add(movies[i])
            if (i < series.size) out.add(series[i])
        }
        return out
    }

    private fun preloadTopSeries() {
        if (seriesPreloadStarted || vodSeries.isEmpty()) return
        seriesPreloadStarted = true
        val queue = vodSeries.take(60).map { it.id }
        val index = AtomicInteger(0)
        viewModelScope.launch {
            delay(300)
            val workers = (0 until 5).map {
                async {
                    while (true) {
                        val i = index.getAndIncrement()
                        if (i >= queue.size) break
                        val sid = queue[i]
                        if (!seriesInfoMemoryCache.containsKey(sid)) {
                            val disk = prefs.readSeriesInfoCache(sid)
                            if (disk != null) {
                                seriesInfoMemoryCache[sid] = disk
                            } else {
                                runCatching {
                                    val fetched = repo.fetchSeriesInfo(sid)
                                    seriesInfoMemoryCache[sid] = fetched
                                    prefs.writeSeriesInfoCache(sid, fetched)
                                }
                            }
                        }
                    }
                }
            }
            workers.awaitAll()
        }
    }

    fun onVodItemClick(item: VodItem) {
        if (item.type == VodType.MOVIE) {
            val url = MediaConfig.vodMovieUrl(item.id, item.ext)
            val strings = AppStrings.forLang(_uiState.value.activeLang)
            _uiState.update {
                it.copy(
                    selectedSeries = null,
                    currentScreen = AppScreen.VOD_PLAYER,
                    vodPlayerUrl = url,
                    vodPlayerTitle = item.title,
                    vodPlayerSub = strings.movieBadge
                )
            }
        } else {
            openSeriesDetail(item)
        }
    }

    fun openSeriesDetail(series: VodItem) {
        _uiState.update {
            it.copy(
                selectedSeries = series,
                currentScreen = AppScreen.EPISODES,
                episodesError = null
            )
        }
        val cachedMem = seriesInfoMemoryCache[series.id]
        if (cachedMem != null) {
            _uiState.update { it.copy(episodesBySeason = cachedMem, episodesLoading = false) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(episodesLoading = true, episodesBySeason = emptyList()) }
            val cachedDisk = prefs.readSeriesInfoCache(series.id)
            if (cachedDisk != null) {
                seriesInfoMemoryCache[series.id] = cachedDisk
                _uiState.update { it.copy(episodesBySeason = cachedDisk, episodesLoading = false) }
                return@launch
            }
            try {
                val fetched = repo.fetchSeriesInfo(series.id)
                seriesInfoMemoryCache[series.id] = fetched
                prefs.writeSeriesInfoCache(series.id, fetched)
                _uiState.update { it.copy(episodesBySeason = fetched, episodesLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(episodesLoading = false, episodesError = e.message ?: "Error")
                }
            }
        }
    }

    fun retrySeriesInfo() {
        val series = _uiState.value.selectedSeries ?: return
        seriesInfoMemoryCache.remove(series.id)
        openSeriesDetail(series)
    }

    fun playEpisode(seasonNumber: Int, episode: VodEpisode) {
        val series = _uiState.value.selectedSeries
        val strings = AppStrings.forLang(_uiState.value.activeLang)
        val url = MediaConfig.vodEpisodeUrl(episode.id, episode.ext)
        val sub = buildString {
            if (series != null) {
                append(series.title)
                append(" • ")
            }
            append("${strings.season} $seasonNumber • ${strings.episode} ${episode.episodeNum}")
        }
        _uiState.update {
            it.copy(
                currentScreen = AppScreen.VOD_PLAYER,
                vodPlayerUrl = url,
                vodPlayerTitle = episode.title.ifBlank { "${strings.episode} ${episode.episodeNum}" },
                vodPlayerSub = sub
            )
        }
    }
}
