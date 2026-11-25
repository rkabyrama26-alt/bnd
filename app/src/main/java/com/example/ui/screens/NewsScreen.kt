package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.localization.StringPack
import com.example.model.NewsArticle
import com.example.ui.components.BackCircleButton
import com.example.ui.components.ScreenOverlayContainer
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.PurpleMuted
import com.example.ui.theme.PurpleSoft
import com.example.ui.theme.TajawalFontFamily
import com.example.viewmodel.MainUiState
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun NewsScreen(
    uiState: MainUiState,
    strings: StringPack,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    ScreenOverlayContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                BackCircleButton(onClick = onBack, testTag = "news_back_btn")
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    uiState.newsLoading -> {
                        Text(
                            text = strings.loadingNews,
                            color = PurpleSoft,
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 40.dp)
                        )
                    }
                    uiState.newsError != null -> {
                        Text(
                            text = "${strings.errorLoad}\n${uiState.newsError}",
                            color = ErrorRed,
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 40.dp)
                        )
                    }
                    uiState.news.isEmpty() -> {
                        Text(
                            text = strings.noContent,
                            color = PurpleSoft,
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 40.dp)
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            itemsIndexed(
                                items = uiState.news,
                                key = { idx, item -> "${idx}_${item.title.take(16)}" }
                            ) { _, article ->
                                NewsCard(
                                    article = article,
                                    strings = strings,
                                    activeLang = uiState.activeLang
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
private fun NewsCard(
    article: NewsArticle,
    strings: StringPack,
    activeLang: String
) {
    val cardBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.08f),
            Color.White.copy(alpha = 0.02f)
        )
    )
    val dateLabel = remember(article.publishedIso, activeLang) {
        formatNewsDate(article.publishedIso, activeLang)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(18.dp), ambientColor = Color.Black.copy(alpha = 0.4f))
            .clip(RoundedCornerShape(18.dp))
            .background(cardBrush)
    ) {
        if (article.imageUrl.isNotBlank()) {
            AsyncImage(
                model = article.imageUrl,
                contentDescription = article.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xFF150E26))
            )
        }

        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = article.title,
                color = Color.White,
                fontFamily = TajawalFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                lineHeight = 21.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (article.description.isNotBlank()) {
                Text(
                    text = article.description,
                    color = PurpleSoft,
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.appSourceName,
                    color = PurpleLight,
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                if (dateLabel.isNotBlank()) {
                    Text(
                        text = dateLabel,
                        color = PurpleMuted,
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

private fun formatNewsDate(iso: String, lang: String): String {
    if (iso.isBlank()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(iso) ?: return ""
        val outLocale = if (lang == "ar") Locale("ar", "EG") else Locale.US
        SimpleDateFormat("MMM d, hh:mm a", outLocale).format(date)
    } catch (_: Exception) {
        ""
    }
}
