package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.localization.StringPack
import com.example.model.AppScreen
import com.example.ui.components.CalendarVectorIcon
import com.example.ui.components.ClapperboardVectorIcon
import com.example.ui.components.GlassButtonSurface
import com.example.ui.components.NewsVectorIcon
import com.example.ui.components.TvPlayVectorIcon
import com.example.ui.theme.GlassBlueBorder
import com.example.ui.theme.GlassBlueEnd
import com.example.ui.theme.GlassBlueStart
import com.example.ui.theme.GlassDarkBorder
import com.example.ui.theme.GlassDarkEnd
import com.example.ui.theme.GlassDarkStart
import com.example.ui.theme.GlassPillBorder
import com.example.ui.theme.GlassPillEnd
import com.example.ui.theme.GlassPillMid
import com.example.ui.theme.GlassPillStart
import com.example.ui.theme.GlassSmokeBorder
import com.example.ui.theme.GlassSmokeEnd
import com.example.ui.theme.GlassSmokeStart
import com.example.ui.theme.TajawalFontFamily

@Composable
fun HomeScreen(
    strings: StringPack,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(15.dp),
        contentAlignment = Alignment.Center
    ) {
        val isCompactHeight = maxHeight < 650.dp
        val circleSize = if (isCompactHeight) 80.dp else 90.dp
        val pillHeight = if (isCompactHeight) 80.dp else 90.dp
        val iconSize = if (isCompactHeight) 36.dp else 42.dp
        val vGap = if (isCompactHeight) 15.dp else 22.dp
        val hGap = if (isCompactHeight) 20.dp else 25.dp
        val gridMaxWidth = if (isCompactHeight) 280.dp else 320.dp
        val labelFontSize = if (isCompactHeight) 12.sp else 14.sp
        val pillTextSize = if (isCompactHeight) 18.sp else 20.sp

        val blueBrush = Brush.linearGradient(
            colors = listOf(GlassBlueStart, GlassBlueEnd)
        )
        val darkBrush = Brush.linearGradient(
            colors = listOf(GlassDarkStart, GlassDarkEnd)
        )
        val pillBrush = Brush.horizontalGradient(
            colors = listOf(GlassPillStart, GlassPillMid, GlassPillEnd)
        )
        val smokeBrush = Brush.linearGradient(
            colors = listOf(GlassSmokeStart, GlassSmokeEnd)
        )

        Column(
            modifier = Modifier.widthIn(max = gridMaxWidth).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(vGap),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Row 1: Channels & Schedule
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(hGap)
            ) {
                CircleGlassTile(
                    label = strings.channels,
                    labelFontSize = labelFontSize,
                    circleSize = circleSize,
                    brush = blueBrush,
                    borderColor = GlassBlueBorder,
                    testTag = "home_channels_btn",
                    onClick = { onNavigate(AppScreen.CHANNELS) },
                    modifier = Modifier.weight(1f)
                ) {
                    TvPlayVectorIcon(modifier = Modifier.size(iconSize))
                }

                CircleGlassTile(
                    label = strings.schedule,
                    labelFontSize = labelFontSize,
                    circleSize = circleSize,
                    brush = darkBrush,
                    borderColor = GlassDarkBorder,
                    testTag = "home_schedule_btn",
                    onClick = { onNavigate(AppScreen.SCHEDULE) },
                    modifier = Modifier.weight(1f)
                ) {
                    CalendarVectorIcon(modifier = Modifier.size(iconSize))
                }
            }

            // Row 2: Settings Pill (spans full width)
            GlassButtonSurface(
                shape = RoundedCornerShape(pillHeight / 2),
                backgroundBrush = pillBrush,
                borderColor = GlassPillBorder,
                onClick = { onNavigate(AppScreen.SETTINGS) },
                testTag = "home_settings_btn",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(pillHeight)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Text(
                        text = strings.settings,
                        color = Color.White,
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = pillTextSize,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(0f, 1f),
                                blurRadius = 3f
                            )
                        )
                    )
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = strings.settings,
                        tint = Color.White,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }

            // Row 3: News & Series
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(hGap)
            ) {
                CircleGlassTile(
                    label = strings.news,
                    labelFontSize = labelFontSize,
                    circleSize = circleSize,
                    brush = smokeBrush,
                    borderColor = GlassSmokeBorder,
                    testTag = "home_news_btn",
                    onClick = { onNavigate(AppScreen.NEWS) },
                    modifier = Modifier.weight(1f)
                ) {
                    NewsVectorIcon(modifier = Modifier.size(iconSize))
                }

                CircleGlassTile(
                    label = strings.series,
                    labelFontSize = labelFontSize,
                    circleSize = circleSize,
                    brush = blueBrush,
                    borderColor = GlassBlueBorder,
                    testTag = "home_series_btn",
                    onClick = { onNavigate(AppScreen.VOD) },
                    modifier = Modifier.weight(1f)
                ) {
                    ClapperboardVectorIcon(modifier = Modifier.size(iconSize))
                }
            }
        }
    }
}

@Composable
private fun CircleGlassTile(
    label: String,
    labelFontSize: androidx.compose.ui.unit.TextUnit,
    circleSize: Dp,
    brush: Brush,
    borderColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        GlassButtonSurface(
            shape = CircleShape,
            backgroundBrush = brush,
            borderColor = borderColor,
            onClick = onClick,
            testTag = testTag,
            modifier = Modifier.size(circleSize)
        ) {
            Box(contentAlignment = Alignment.Center) {
                icon()
            }
        }
        Text(
            text = label,
            color = Color.White,
            fontFamily = TajawalFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = labelFontSize,
            lineHeight = labelFontSize,
            textAlign = TextAlign.Center,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.8f),
                    offset = Offset(0f, 2f),
                    blurRadius = 6f
                )
            )
        )
    }
}
