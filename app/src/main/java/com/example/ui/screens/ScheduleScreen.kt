package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.localization.StringPack
import com.example.model.MatchEvent
import com.example.model.MatchState
import com.example.ui.components.BackCircleButton
import com.example.ui.components.ScreenOverlayContainer
import com.example.ui.theme.DividerPurple
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LiveRedText
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.PurpleSoft
import com.example.ui.theme.ScheduledBlue
import com.example.ui.theme.TajawalFontFamily
import com.example.ui.theme.TextLav
import com.example.viewmodel.MainUiState
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun ScheduleScreen(
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
                BackCircleButton(onClick = onBack, testTag = "schedule_back_btn")
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    uiState.scheduleLoading -> {
                        Text(
                            text = strings.loadingMatches,
                            color = PurpleSoft,
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 40.dp)
                        )
                    }
                    uiState.scheduleError != null -> {
                        Text(
                            text = "${strings.errorLoad}\n${uiState.scheduleError}",
                            color = ErrorRed,
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 40.dp)
                        )
                    }
                    uiState.matches.isEmpty() -> {
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
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = uiState.matches,
                                key = { it.id }
                            ) { match ->
                                MatchCard(
                                    match = match,
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
private fun MatchCard(
    match: MatchEvent,
    strings: StringPack,
    activeLang: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val liveAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_alpha"
    )

    val (badgeBg, badgeTextColor, badgeLabel) = when (match.state) {
        MatchState.LIVE -> Triple(
            Color(0x33FF3C3C),
            LiveRedText,
            strings.statusLive
        )
        MatchState.FINAL -> Triple(
            Color(0x26A0A0A0),
            Color(0xFFAAAAAA),
            strings.statusFinal
        )
        MatchState.SCHEDULED -> Triple(
            Color(0x2664C8FF),
            ScheduledBlue,
            strings.statusUpcoming
        )
    }

    val cardBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.08f),
            Color.White.copy(alpha = 0.02f)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(18.dp), ambientColor = Color.Black.copy(alpha = 0.4f))
            .clip(RoundedCornerShape(18.dp))
            .background(cardBrush)
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 12.dp)
                .then(if (match.state == MatchState.LIVE) Modifier.alpha(liveAlpha) else Modifier)
                .clip(RoundedCornerShape(20.dp))
                .background(badgeBg)
                .padding(horizontal = 10.dp, vertical = 3.dp)
        ) {
            Text(
                text = badgeLabel,
                color = badgeTextColor,
                fontFamily = TajawalFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 10.sp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TeamColumn(
                name = match.homeName,
                logoUrl = match.homeLogo,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier.widthIn(min = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                if (match.state == MatchState.SCHEDULED) {
                    val (timeStr, dateStr) = remember(match.dateIso, activeLang) {
                        formatMatchDateTime(match.dateIso, activeLang)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = timeStr,
                            color = Color.White,
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dateStr,
                            color = PurpleSoft,
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = match.homeScore,
                            color = if (match.homeWinner) PurpleLight else Color.White,
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.8f),
                                    offset = Offset(0f, 2f),
                                    blurRadius = 8f
                                )
                            )
                        )
                        Text(
                            text = ":",
                            color = DividerPurple,
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Text(
                            text = match.awayScore,
                            color = if (match.awayWinner) PurpleLight else Color.White,
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.8f),
                                    offset = Offset(0f, 2f),
                                    blurRadius = 8f
                                )
                            )
                        )
                    }
                }
            }

            TeamColumn(
                name = match.awayName,
                logoUrl = match.awayLogo,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TeamColumn(
    name: String,
    logoUrl: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AsyncImage(
            model = logoUrl,
            contentDescription = name,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(55.dp)
        )
        Text(
            text = name,
            color = TextLav,
            fontFamily = TajawalFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 110.dp)
        )
    }
}

private fun formatMatchDateTime(iso: String, lang: String): Pair<String, String> {
    if (iso.isBlank()) return "--:--" to ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(iso) ?: run {
            val parserWithSec = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            parserWithSec.parse(iso)
        } ?: return "--:--" to ""

        val outLocale = if (lang == "ar") Locale("ar", "EG") else Locale.US
        val timeFmt = SimpleDateFormat("hh:mm a", outLocale)
        val dateFmt = SimpleDateFormat("EEEE d MMMM", outLocale)
        timeFmt.format(date) to dateFmt.format(date)
    } catch (_: Exception) {
        "--:--" to ""
    }
}
