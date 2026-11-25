package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.localization.StringPack
import com.example.model.VodItem
import com.example.model.VodType
import com.example.ui.components.BackCircleButton
import com.example.ui.components.GlassSearchBar
import com.example.ui.components.ScreenOverlayContainer
import com.example.ui.theme.GoldStar
import com.example.ui.theme.MovieBadgeEnd
import com.example.ui.theme.MovieBadgeStart
import com.example.ui.theme.PurpleDeep
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleSoft
import com.example.ui.theme.SeriesBadgeEnd
import com.example.ui.theme.SeriesBadgeStart
import com.example.ui.theme.TajawalFontFamily
import com.example.viewmodel.MainUiState

@Composable
fun VodScreen(
    uiState: MainUiState,
    strings: StringPack,
    onSelectFilter: (VodType) -> Unit,
    onSearchChange: (String) -> Unit,
    onItemClick: (VodItem) -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val filteredList = remember(uiState.vodItems, uiState.vodFilter, uiState.vodSearchQuery) {
        val byType = uiState.vodItems.filter { it.type == uiState.vodFilter }
        val q = uiState.vodSearchQuery.trim().lowercase()
        if (q.isEmpty()) byType else byType.filter { it.title.lowercase().contains(q) }
    }

    ScreenOverlayContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            // Centered Pill Tabs + Left Back Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0x8018142F), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    VodTabButton(
                        label = strings.series,
                        isActive = uiState.vodFilter == VodType.SERIES,
                        onClick = { onSelectFilter(VodType.SERIES) },
                        testTag = "vod_tab_series"
                    )
                    VodTabButton(
                        label = strings.movies,
                        isActive = uiState.vodFilter == VodType.MOVIE,
                        onClick = { onSelectFilter(VodType.MOVIE) },
                        testTag = "vod_tab_movies"
                    )
                }

                BackCircleButton(
                    onClick = onBack,
                    testTag = "vod_back_btn",
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }

            GlassSearchBar(
                query = uiState.vodSearchQuery,
                onQueryChange = onSearchChange,
                placeholder = strings.search,
                testTag = "vod_search_input",
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
            )

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val columns = when {
                    maxWidth >= 500.dp -> 4
                    maxWidth <= 340.dp -> 2
                    else -> 3
                }

                when {
                    uiState.vodLoading && uiState.vodItems.isEmpty() -> {
                        VodStatusBox(
                            title = strings.loadingVod,
                            showRetry = false,
                            retryLabel = strings.retry,
                            onRetry = onRetry
                        )
                    }
                    uiState.vodError == true -> {
                        VodStatusBox(
                            title = "❌ ${strings.errorLoad}\n${strings.errorRetry}",
                            showRetry = true,
                            retryLabel = strings.retry,
                            onRetry = onRetry
                        )
                    }
                    filteredList.isEmpty() -> {
                        VodStatusBox(
                            title = strings.noContent,
                            showRetry = false,
                            retryLabel = strings.retry,
                            onRetry = onRetry
                        )
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(
                                items = filteredList,
                                key = { idx, item -> "${item.type}_${item.id}_$idx" }
                            ) { _, item ->
                                VodPosterCard(
                                    item = item,
                                    strings = strings,
                                    onClick = { onItemClick(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VodTabButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(40.dp))
            .then(
                if (isActive) {
                    Modifier.background(Brush.linearGradient(listOf(PurplePrimary, PurpleDeep)))
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 9.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isActive) Color.White else Color(0xFF8B7EB0),
            fontFamily = TajawalFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun VodPosterCard(
    item: VodItem,
    strings: StringPack,
    onClick: () -> Unit
) {
    val cardBg = Brush.linearGradient(
        listOf(PurplePrimary.copy(alpha = 0.25f), PurpleDeep.copy(alpha = 0.10f))
    )
    val badgeBrush = if (item.type == VodType.MOVIE) {
        Brush.linearGradient(listOf(MovieBadgeStart, MovieBadgeEnd))
    } else {
        Brush.linearGradient(listOf(SeriesBadgeStart, SeriesBadgeEnd))
    }
    val ratingVal = item.rating.toDoubleOrNull()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f / 3f)
            .shadow(8.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = 0.5f))
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("vod_card_${item.id}")
    ) {
        val fallbackEmoji = if (item.type == VodType.MOVIE) "🎬" else "📺"
        if (item.poster.isNotBlank()) {
            SubcomposeAsyncImage(
                model = item.poster,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                error = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = fallbackEmoji, fontSize = 36.sp)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = fallbackEmoji, fontSize = 36.sp)
            }
        }

        // Bottom dark gradient overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize(0.6f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        0.45f to Color(0x990A0618),
                        1.0f to Color(0xF20A0618)
                    )
                )
        )

        // Type badge (top-start in RTL = top-right visually)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(badgeBrush)
                .padding(horizontal = 9.dp, vertical = 3.dp)
        ) {
            Text(
                text = if (item.type == VodType.MOVIE) strings.movieBadge else strings.seriesBadge,
                color = Color.White,
                fontFamily = TajawalFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 9.sp
            )
        }

        // Rating badge (top-end in RTL = top-left visually)
        if (ratingVal != null && ratingVal > 0.0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "★ ${String.format("%.1f", ratingVal)}",
                    color = GoldStar,
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp
                )
            }
        }

        // Title at bottom
        Text(
            text = item.title,
            color = Color.White,
            fontFamily = TajawalFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.8f),
                    offset = Offset(0f, 1f),
                    blurRadius = 3f
                )
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, bottom = 10.dp, top = 8.dp)
        )
    }
}

@Composable
private fun VodStatusBox(
    title: String,
    showRetry: Boolean,
    retryLabel: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .padding(horizontal = 20.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = PurpleSoft,
            fontFamily = TajawalFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        if (showRetry) {
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(PurplePrimary.copy(alpha = 0.85f), PurpleDeep.copy(alpha = 0.85f))
                        )
                    )
                    .clickable(onClick = onRetry)
                    .padding(horizontal = 24.dp, vertical = 10.dp)
                    .testTag("vod_retry_btn")
            ) {
                Text(
                    text = retryLabel,
                    color = Color.White,
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
