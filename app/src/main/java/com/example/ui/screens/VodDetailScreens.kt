package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.StringPack
import com.example.model.VodEpisode
import com.example.ui.components.BackCircleButton
import com.example.ui.components.NetflixVideoPlayer
import com.example.ui.components.ScreenOverlayContainer
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PurpleDeep
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleSoft
import com.example.ui.theme.PurpleTitle
import com.example.ui.theme.TajawalFontFamily
import com.example.ui.theme.TextLav
import com.example.viewmodel.MainUiState

@Composable
fun EpisodesScreen(
    uiState: MainUiState,
    strings: StringPack,
    onPlayEpisode: (Int, VodEpisode) -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val seriesTitle = uiState.selectedSeries?.title ?: strings.series

    ScreenOverlayContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = seriesTitle,
                    color = Color.White,
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                BackCircleButton(onClick = onBack, testTag = "episodes_back_btn")
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    uiState.episodesLoading -> {
                        Text(
                            text = strings.loadingEpisodes,
                            color = PurpleSoft,
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 30.dp)
                        )
                    }
                    uiState.episodesError != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.TopCenter).padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = strings.errorLoad,
                                color = ErrorRed,
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Brush.linearGradient(listOf(PurplePrimary, PurpleDeep)))
                                    .clickable(onClick = onRetry)
                                    .padding(horizontal = 24.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = strings.retry,
                                    color = Color.White,
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                    uiState.episodesBySeason.isEmpty() -> {
                        Text(
                            text = strings.noContent,
                            color = PurpleSoft,
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 30.dp)
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            uiState.episodesBySeason.forEach { season ->
                                item(key = "season_${season.seasonNumber}") {
                                    Column(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) {
                                        Text(
                                            text = "${strings.season} ${season.seasonNumber}",
                                            color = PurpleTitle,
                                            fontFamily = TajawalFontFamily,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(42.dp)
                                                .height(3.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(
                                                    Brush.horizontalGradient(
                                                        listOf(PurplePrimary, Color.Transparent)
                                                    )
                                                )
                                        )
                                    }
                                }

                                items(
                                    items = season.episodes,
                                    key = { ep -> "ep_${season.seasonNumber}_${ep.id}" }
                                ) { ep ->
                                    val epTitle = ep.title.ifBlank { "${strings.episode} ${ep.episodeNum}" }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .shadow(6.dp, RoundedCornerShape(14.dp), ambientColor = Color.Black.copy(alpha = 0.35f))
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(
                                                        PurplePrimary.copy(alpha = 0.15f),
                                                        PurpleDeep.copy(alpha = 0.06f)
                                                    )
                                                )
                                            )
                                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
                                            .clickable { onPlayEpisode(season.seasonNumber, ep) }
                                            .padding(horizontal = 16.dp, vertical = 14.dp)
                                            .testTag("episode_item_${ep.id}"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Brush.linearGradient(listOf(PurplePrimary, PurpleDeep))),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = ep.episodeNum,
                                                color = Color.White,
                                                fontFamily = TajawalFontFamily,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 13.sp
                                            )
                                        }

                                        Text(
                                            text = epTitle,
                                            color = TextLav,
                                            fontFamily = TajawalFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Text(
                                            text = "▶",
                                            color = PurpleLight,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VodPlayerScreen(
    uiState: MainUiState,
    strings: StringPack,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    onToggleStretch: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler {
        if (isFullscreen) onToggleFullscreen() else onBack()
    }

    ScreenOverlayContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (isFullscreen) Modifier else Modifier.safeDrawingPadding())
        ) {
            NetflixVideoPlayer(
                streamUrl = uiState.vodPlayerUrl,
                title = uiState.vodPlayerTitle,
                isLive = false,
                liveBadgeText = strings.liveBadge,
                isStretched = uiState.vodStretch,
                isFullscreen = isFullscreen,
                onToggleStretch = onToggleStretch,
                onToggleFullscreen = onToggleFullscreen,
                onBackClick = {
                    if (isFullscreen) onToggleFullscreen() else onBack()
                },
                modifier = if (isFullscreen) Modifier.fillMaxSize() else Modifier.fillMaxWidth()
            )

            if (!isFullscreen) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(16.dp)
                ) {
                    Text(
                        text = uiState.vodPlayerTitle.ifBlank { "—" },
                        color = Color.White,
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        lineHeight = 22.sp
                    )
                    if (uiState.vodPlayerSub.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = uiState.vodPlayerSub,
                            color = PurpleSoft,
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
