package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.R
import com.example.data.MediaConfig
import com.example.localization.AppStrings
import com.example.localization.StringPack
import com.example.model.LiveChannel
import com.example.ui.components.GlassSearchBar
import com.example.ui.components.NetflixVideoPlayer
import com.example.ui.components.ScreenOverlayContainer
import com.example.ui.theme.CatActiveBg
import com.example.ui.theme.CatPanelBg
import com.example.ui.theme.ChannelActiveBg
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.FavActiveEnd
import com.example.ui.theme.FavActiveStart
import com.example.ui.theme.HeartRed
import com.example.ui.theme.PinkFav
import com.example.ui.theme.PurpleMuted
import com.example.ui.theme.PurpleSoft
import com.example.ui.theme.TajawalFontFamily
import com.example.ui.theme.TextLav
import com.example.viewmodel.MainUiState

@Composable
fun ChannelsScreen(
    uiState: MainUiState,
    strings: StringPack,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    onToggleStretch: () -> Unit,
    onSelectCategory: (String) -> Unit,
    onSelectChannel: (LiveChannel) -> Unit,
    onToggleFavorite: (LiveChannel) -> Unit,
    onSearchChange: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler {
        if (isFullscreen) onToggleFullscreen() else onBack()
    }

    val activeChannel = uiState.activeChannel
    val streamUrl = activeChannel?.let { MediaConfig.liveStreamUrl(it.streamId) }
    val channelTitle = activeChannel?.let { AppStrings.getArabicChannelName(it.name) } ?: "—"

    val displayedChannels = remember(
        uiState.currentCategory,
        uiState.allChannels,
        uiState.categoryCache,
        uiState.favoriteChannels,
        uiState.channelSearchQuery
    ) {
        val base = when {
            uiState.currentCategory == "__favorites__" -> uiState.favoriteChannels.values.toList()
            uiState.currentCategory == "all" -> uiState.allChannels
            uiState.categoryCache.containsKey(uiState.currentCategory) ->
                uiState.categoryCache[uiState.currentCategory].orEmpty()
            else -> uiState.allChannels.filter { it.categoryId == uiState.currentCategory.trim() }
        }

        val query = uiState.channelSearchQuery.trim().lowercase()
        if (query.isEmpty()) {
            base
        } else {
            base.filter { ch ->
                val arName = AppStrings.getArabicChannelName(ch.name).lowercase()
                val orig = ch.name.lowercase()
                arName.contains(query) || orig.contains(query)
            }
        }
    }

    ScreenOverlayContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (isFullscreen) Modifier else Modifier.safeDrawingPadding())
        ) {
            NetflixVideoPlayer(
                streamUrl = streamUrl,
                title = channelTitle,
                isLive = true,
                liveBadgeText = strings.liveBadge,
                isStretched = uiState.chStretch,
                isFullscreen = isFullscreen,
                onToggleStretch = onToggleStretch,
                onToggleFullscreen = onToggleFullscreen,
                onBackClick = {
                    if (isFullscreen) onToggleFullscreen() else onBack()
                },
                modifier = if (isFullscreen) Modifier.fillMaxSize() else Modifier.fillMaxWidth()
            )

            if (!isFullscreen) {
                GlassSearchBar(
                    query = uiState.channelSearchQuery,
                    onQueryChange = onSearchChange,
                    placeholder = strings.search,
                    testTag = "channels_search_input",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // Channels list (first in RTL -> Right side, matching HTML DOM order)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        when {
                            uiState.channelsLoading || uiState.channelsCategoryLoading -> {
                                StatusMessage(
                                    text = strings.loadingChannels,
                                    color = PurpleSoft
                                )
                            }
                            uiState.channelsError != null && displayedChannels.isEmpty() -> {
                                StatusMessage(
                                    text = "${strings.errorLoad}\n${uiState.channelsError}",
                                    color = ErrorRed
                                )
                            }
                            displayedChannels.isEmpty() -> {
                                if (uiState.currentCategory == "__favorites__") {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 40.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = strings.noFavoritesTitle,
                                            color = PinkFav,
                                            fontFamily = TajawalFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = strings.noFavoritesHint,
                                            color = PinkFav.copy(alpha = 0.75f),
                                            fontFamily = TajawalFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    StatusMessage(
                                        text = strings.noContent,
                                        color = PurpleSoft
                                    )
                                }
                            }
                            else -> {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    itemsIndexed(
                                        items = displayedChannels,
                                        key = { idx, item -> "${item.streamId}_$idx" }
                                    ) { index, channel ->
                                        val isActive = activeChannel?.streamId == channel.streamId
                                        val isFav = uiState.favoriteChannels.containsKey(channel.streamId)
                                        ChannelRow(
                                            index = index + 1,
                                            channel = channel,
                                            isActive = isActive,
                                            isFavorite = isFav,
                                            onClick = { onSelectChannel(channel) },
                                            onToggleFavorite = { onToggleFavorite(channel) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Categories sidebar (width 110dp, matching HTML .cat-list)
                    Box(
                        modifier = Modifier
                            .width(110.dp)
                            .fillMaxHeight()
                            .background(CatPanelBg)
                    ) {
                        if (uiState.channelsLoading) {
                            StatusMessage(text = strings.loading, color = PurpleSoft)
                        } else if (uiState.channelsError != null && uiState.categories.isEmpty()) {
                            StatusMessage(text = strings.errorLoad, color = ErrorRed)
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                item(key = "__favorites__") {
                                    val favActive = uiState.currentCategory == "__favorites__"
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .then(
                                                if (favActive) {
                                                    Modifier.background(
                                                        Brush.linearGradient(
                                                            listOf(FavActiveStart, FavActiveEnd)
                                                        )
                                                    )
                                                } else {
                                                    Modifier
                                                }
                                            )
                                            .clickable { onSelectCategory("__favorites__") }
                                            .padding(horizontal = 8.dp, vertical = 16.dp)
                                            .testTag("cat_favorites"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = "❤️", fontSize = 14.sp)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = strings.favorites,
                                                color = if (favActive) Color.White else PinkFav,
                                                fontFamily = TajawalFontFamily,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 12.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                    HorizontalDivider(color = HeartRed.copy(alpha = 0.15f))
                                }

                                item(key = "all") {
                                    CategoryItem(
                                        name = strings.all,
                                        isActive = uiState.currentCategory == "all",
                                        onClick = { onSelectCategory("all") }
                                    )
                                }

                                itemsIndexed(
                                    items = uiState.categories,
                                    key = { idx, cat -> "${cat.categoryId}_$idx" }
                                ) { _, category ->
                                    CategoryItem(
                                        name = category.categoryName,
                                        isActive = uiState.currentCategory == category.categoryId,
                                        onClick = { onSelectCategory(category.categoryId) }
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

@Composable
private fun CategoryItem(
    name: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isActive) CatActiveBg else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            color = if (isActive) Color.White else PurpleSoft,
            fontFamily = TajawalFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ChannelRow(
    index: Int,
    channel: LiveChannel,
    isActive: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isActive) ChannelActiveBg else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("channel_item_${channel.streamId}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = index.toString(),
            color = PurpleMuted,
            fontFamily = TajawalFontFamily,
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(24.dp)
        )

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            if (channel.streamIcon.isNotBlank()) {
                SubcomposeAsyncImage(
                    model = channel.streamIcon,
                    contentDescription = channel.name,
                    contentScale = ContentScale.Fit,
                    error = {
                        Text(text = "📺", fontSize = 16.sp)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(text = "📺", fontSize = 16.sp)
            }
        }

        Text(
            text = AppStrings.getArabicChannelName(channel.name),
            color = TextLav,
            fontFamily = TajawalFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier
                .size(36.dp)
                .testTag("channel_fav_${channel.streamId}")
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = stringResource(R.string.favorite),
                tint = if (isFavorite) HeartRed else Color.White.copy(alpha = 0.35f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun StatusMessage(
    text: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontFamily = TajawalFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}
