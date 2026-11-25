package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ScreenStarColorScheme = darkColorScheme(
    primary = PurplePrimary,
    onPrimary = Color.White,
    secondary = PurpleLight,
    onSecondary = BgDark,
    tertiary = PinkFav,
    background = BgDark,
    onBackground = Color.White,
    surface = ModalBg,
    onSurface = Color.White,
    surfaceVariant = CatPanelBg,
    onSurfaceVariant = PurpleSoft,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun ScreenStarTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ScreenStarColorScheme,
        typography = Typography,
        content = content
    )
}
