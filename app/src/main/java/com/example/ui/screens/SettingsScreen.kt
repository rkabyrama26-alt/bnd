package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.PlayCircleFilled
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.localization.StringPack
import com.example.ui.components.BackCircleButton
import com.example.ui.components.ScreenOverlayContainer
import com.example.ui.theme.EmailRed
import com.example.ui.theme.ModalBg
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleSoft
import com.example.ui.theme.TajawalFontFamily
import com.example.ui.theme.TelegramBlue
import com.example.ui.theme.YoutubeRed
import com.example.viewmodel.MainUiState

@Composable
fun SettingsScreen(
    uiState: MainUiState,
    strings: StringPack,
    onOpenLanguageModal: () -> Unit,
    onCloseLanguageModal: () -> Unit,
    onSelectLanguage: (String) -> Unit,
    onOpenSupportModal: () -> Unit,
    onCloseSupportModal: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    BackHandler {
        when {
            uiState.isLanguageModalOpen -> onCloseLanguageModal()
            uiState.isSupportModalOpen -> onCloseSupportModal()
            else -> onBack()
        }
    }

    ScreenOverlayContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                BackCircleButton(onClick = onBack, testTag = "settings_back_btn")
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                SettingsActionCard(
                    title = strings.language,
                    icon = Icons.Rounded.Language,
                    iconBg = TelegramBlue.copy(alpha = 0.2f),
                    iconTint = TelegramBlue,
                    onClick = onOpenLanguageModal,
                    testTag = "settings_language_card"
                )

                SettingsActionCard(
                    title = strings.technicalSupport,
                    icon = Icons.Rounded.Feedback,
                    iconBg = PurplePrimary.copy(alpha = 0.2f),
                    iconTint = PurpleLight,
                    onClick = onOpenSupportModal,
                    testTag = "settings_support_card"
                )
            }
        }
    }

    if (uiState.isLanguageModalOpen) {
        LanguagePickerDialog(
            strings = strings,
            selectedSetting = uiState.userLangSetting,
            onSelect = onSelectLanguage,
            onDismiss = onCloseLanguageModal
        )
    }

    if (uiState.isSupportModalOpen) {
        SupportDialog(
            strings = strings,
            onOpenUrl = { url ->
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            },
            onOpenEmail = { email ->
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")))
                }
            },
            onDismiss = onCloseSupportModal
        )
    }
}

@Composable
private fun SettingsActionCard(
    title: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(20.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Text(
                text = title,
                color = Color.White,
                fontFamily = TajawalFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = title,
            tint = PurpleSoft,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun LanguagePickerDialog(
    strings: StringPack,
    selectedSetting: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        "auto" to strings.autoLang,
        "ar" to "العربية",
        "en" to "English",
        "fa" to "فارسی",
        "ku" to "کوردی"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(onClick = onDismiss)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 380.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(ModalBg)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                    .clickable(enabled = false) {}
                    .padding(24.dp)
            ) {
                ModalHeader(
                    title = strings.language,
                    icon = Icons.Rounded.Language,
                    onDismiss = onDismiss
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    options.forEach { (code, label) ->
                        val active = selectedSetting == code
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (active) PurplePrimary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
                                )
                                .border(
                                    1.dp,
                                    if (active) PurplePrimary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.05f),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { onSelect(code) }
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                                .testTag("lang_option_$code"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                color = Color.White,
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            if (active) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = label,
                                    tint = PurpleLight,
                                    modifier = Modifier.size(20.dp)
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
private fun SupportDialog(
    strings: StringPack,
    onOpenUrl: (String) -> Unit,
    onOpenEmail: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(onClick = onDismiss)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 380.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(ModalBg)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                    .clickable(enabled = false) {}
                    .padding(24.dp)
            ) {
                ModalHeader(
                    title = strings.technicalSupport,
                    icon = Icons.Rounded.Feedback,
                    onDismiss = onDismiss
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SocialItemRow(
                        label = strings.telegram,
                        icon = Icons.AutoMirrored.Rounded.Send,
                        iconBg = TelegramBlue.copy(alpha = 0.15f),
                        iconTint = TelegramBlue,
                        onClick = { onOpenUrl("https://t.me/azcajfaj") },
                        testTag = "support_telegram"
                    )
                    SocialItemRow(
                        label = strings.youtube,
                        icon = Icons.Rounded.PlayCircleFilled,
                        iconBg = YoutubeRed.copy(alpha = 0.15f),
                        iconTint = YoutubeRed,
                        onClick = { onOpenUrl("https://youtube.com/@screen.star123?si=0VzNlElGEvNDe2mY") },
                        testTag = "support_youtube"
                    )
                    SocialItemRow(
                        label = strings.email,
                        icon = Icons.Rounded.Email,
                        iconBg = EmailRed.copy(alpha = 0.15f),
                        iconTint = EmailRed,
                        onClick = { onOpenEmail("zjffjxvh8885@gmail.com") },
                        testTag = "support_email"
                    )
                }
            }
        }
    }
}

@Composable
private fun ModalHeader(
    title: String,
    icon: ImageVector,
    onDismiss: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    color = Color.White,
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.close),
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(15.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SocialItemRow(
    label: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            color = Color.White,
            fontFamily = TajawalFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp
        )
    }
}
