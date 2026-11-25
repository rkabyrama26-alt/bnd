package com.example.model

enum class AppScreen {
    HOME,
    CHANNELS,
    SCHEDULE,
    NEWS,
    VOD,
    EPISODES,
    VOD_PLAYER,
    SETTINGS
}

data class LiveCategory(
    val categoryId: String,
    val categoryName: String
)

data class LiveChannel(
    val streamId: String,
    val name: String,
    val streamIcon: String,
    val categoryId: String
)

enum class MatchState {
    LIVE,
    FINAL,
    SCHEDULED
}

data class MatchEvent(
    val id: String,
    val homeName: String,
    val awayName: String,
    val homeLogo: String,
    val awayLogo: String,
    val homeScore: String,
    val awayScore: String,
    val homeWinner: Boolean,
    val awayWinner: Boolean,
    val state: MatchState,
    val dateIso: String
)

data class NewsArticle(
    val title: String,
    val description: String,
    val imageUrl: String,
    val publishedIso: String
)

enum class VodType {
    MOVIE,
    SERIES
}

data class VodItem(
    val type: VodType,
    val id: String,
    val title: String,
    val poster: String,
    val rating: String,
    val ext: String = "mp4"
)

data class VodEpisode(
    val id: String,
    val episodeNum: String,
    val title: String,
    val ext: String
)

data class VodSeason(
    val seasonNumber: Int,
    val episodes: List<VodEpisode>
)
