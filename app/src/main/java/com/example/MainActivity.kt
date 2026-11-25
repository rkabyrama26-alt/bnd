package com.example

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.localization.AppStrings
import com.example.model.AppScreen
import com.example.ui.components.AppBackground
import com.example.ui.screens.ChannelsScreen
import com.example.ui.screens.EpisodesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NewsScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VodPlayerScreen
import com.example.ui.screens.VodScreen
import com.example.ui.theme.ScreenStarTheme
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            val viewModel: MainViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val strings = remember(uiState.activeLang) { AppStrings.forLang(uiState.activeLang) }
            val layoutDirection = if (AppStrings.isRtl(uiState.activeLang)) {
                LayoutDirection.Rtl
            } else {
                LayoutDirection.Ltr
            }

            var isFullscreen by remember { mutableStateOf(false) }
            val context = LocalContext.current
            val activity = context as? Activity

            LaunchedEffect(uiState.currentScreen) {
                if (uiState.currentScreen != AppScreen.CHANNELS && uiState.currentScreen != AppScreen.VOD_PLAYER) {
                    isFullscreen = false
                }
            }

            DisposableEffect(isFullscreen) {
                val window = activity?.window
                if (activity != null && window != null) {
                    val controller = WindowCompat.getInsetsController(window, window.decorView)
                    if (isFullscreen) {
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        controller.hide(WindowInsetsCompat.Type.systemBars())
                    } else {
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        controller.show(WindowInsetsCompat.Type.systemBars())
                    }
                }
                onDispose {}
            }

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                ScreenStarTheme {
                    AppBackground {
                        HomeScreen(
                            strings = strings,
                            onNavigate = viewModel::navigateTo
                        )

                        when (uiState.currentScreen) {
                            AppScreen.HOME -> Unit

                            AppScreen.CHANNELS -> ChannelsScreen(
                                uiState = uiState,
                                strings = strings,
                                isFullscreen = isFullscreen,
                                onToggleFullscreen = { isFullscreen = !isFullscreen },
                                onToggleStretch = viewModel::toggleChannelStretch,
                                onSelectCategory = viewModel::selectCategory,
                                onSelectChannel = viewModel::playChannel,
                                onToggleFavorite = viewModel::toggleFavorite,
                                onSearchChange = viewModel::updateChannelSearchQuery,
                                onBack = {
                                    isFullscreen = false
                                    viewModel.navigateBack()
                                }
                            )

                            AppScreen.SCHEDULE -> ScheduleScreen(
                                uiState = uiState,
                                strings = strings,
                                onBack = viewModel::navigateBack
                            )

                            AppScreen.NEWS -> NewsScreen(
                                uiState = uiState,
                                strings = strings,
                                onBack = viewModel::navigateBack
                            )

                            AppScreen.VOD -> VodScreen(
                                uiState = uiState,
                                strings = strings,
                                onSelectFilter = viewModel::setVodFilter,
                                onSearchChange = viewModel::updateVodSearchQuery,
                                onItemClick = viewModel::onVodItemClick,
                                onRetry = viewModel::reloadVod,
                                onBack = viewModel::navigateBack
                            )

                            AppScreen.EPISODES -> EpisodesScreen(
                                uiState = uiState,
                                strings = strings,
                                onPlayEpisode = viewModel::playEpisode,
                                onRetry = viewModel::retrySeriesInfo,
                                onBack = viewModel::navigateBack
                            )

                            AppScreen.VOD_PLAYER -> VodPlayerScreen(
                                uiState = uiState,
                                strings = strings,
                                isFullscreen = isFullscreen,
                                onToggleFullscreen = { isFullscreen = !isFullscreen },
                                onToggleStretch = viewModel::toggleVodStretch,
                                onBack = {
                                    isFullscreen = false
                                    viewModel.navigateBack()
                                }
                            )

                            AppScreen.SETTINGS -> SettingsScreen(
                                uiState = uiState,
                                strings = strings,
                                onOpenLanguageModal = { viewModel.setLanguageModalOpen(true) },
                                onCloseLanguageModal = { viewModel.setLanguageModalOpen(false) },
                                onSelectLanguage = viewModel::setLanguage,
                                onOpenSupportModal = { viewModel.setSupportModalOpen(true) },
                                onCloseSupportModal = { viewModel.setSupportModalOpen(false) },
                                onBack = viewModel::navigateBack
                            )
                        }
                    }
                }
            }
        }
    }
}
