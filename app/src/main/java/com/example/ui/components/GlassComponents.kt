package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BgDark
import com.example.ui.theme.OverlayBg
import com.example.ui.theme.PurpleMuted
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleSoft
import com.example.ui.theme.TajawalFontFamily

@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .drawBehind {
                val center = Offset(size.width * 0.5f, size.height * 0.4f)
                val radius = maxOf(size.width, size.height) * 0.65f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x2E8E3BEA),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )
            },
        content = content
    )
}

@Composable
fun ScreenOverlayContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OverlayBg),
        content = content
    )
}

@Composable
fun GlassButtonSurface(
    shape: Shape,
    backgroundBrush: Brush,
    borderColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(durationMillis = 160),
        label = "glass_btn_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = 16.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = Color.Black.copy(alpha = 0.5f)
            )
            .clip(shape)
            .background(backgroundBrush)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .drawBehind {
                // Top inner glass gloss highlight (0% -> 45% height)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.30f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height * 0.45f
                    )
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun BackCircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    testTag: String = "back_button"
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(size)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.10f))
            .testTag(testTag)
    ) {
        Canvas(modifier = Modifier.size(22.dp)) {
            val w = this.size.width
            val h = this.size.height
            val stroke = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
            drawLine(
                color = Color.White,
                start = Offset(w * 0.79f, h * 0.5f),
                end = Offset(w * 0.21f, h * 0.5f),
                strokeWidth = stroke.width,
                cap = StrokeCap.Round
            )
            val arrow = Path().apply {
                moveTo(w * 0.5f, h * 0.79f)
                lineTo(w * 0.21f, h * 0.5f)
                lineTo(w * 0.5f, h * 0.21f)
            }
            drawPath(arrow, color = Color.White, style = stroke)
        }
    }
}

@Composable
fun GlassSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    testTag: String,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    val bgColor = if (focused) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.08f)
    val borderColor = if (focused) PurplePrimary.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.10f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = stringResource(R.string.app_name),
            tint = PurpleSoft,
            modifier = Modifier.size(18.dp)
        )
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (query.isEmpty()) {
                Text(
                    text = placeholder,
                    color = PurpleMuted,
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = Color.White,
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                cursorBrush = SolidColor(PurplePrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focused = it.isFocused }
                    .testTag(testTag)
            )
        }
    }
}

// Custom Canvas Icons matching the exact SVG icons from the HTML design

@Composable
fun TvPlayVectorIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(42.dp)) {
        val sx = size.width / 64f
        val sy = size.height / 64f
        val stroke = Stroke(width = 4f * sx, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(8f * sx, 14f * sy),
            size = Size(48f * sx, 32f * sy),
            cornerRadius = CornerRadius(4f * sx, 4f * sy),
            style = stroke
        )
        val play = Path().apply {
            moveTo(26f * sx, 23f * sy)
            lineTo(38f * sx, 30f * sy)
            lineTo(26f * sx, 37f * sy)
            close()
        }
        drawPath(play, color = Color.White)
        drawLine(
            color = Color.White,
            start = Offset(20f * sx, 52f * sy),
            end = Offset(44f * sx, 52f * sy),
            strokeWidth = 4f * sx,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White,
            start = Offset(32f * sx, 46f * sy),
            end = Offset(32f * sx, 52f * sy),
            strokeWidth = 4f * sx,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun CalendarVectorIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(42.dp)) {
        val sx = size.width / 64f
        val sy = size.height / 64f
        val stroke = Stroke(width = 4f * sx, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(12f * sx, 16f * sy),
            size = Size(40f * sx, 38f * sy),
            cornerRadius = CornerRadius(6f * sx, 6f * sy),
            style = stroke
        )
        drawLine(
            color = Color.White,
            start = Offset(12f * sx, 28f * sy),
            end = Offset(52f * sx, 28f * sy),
            strokeWidth = 4f * sx
        )
        drawLine(
            color = Color.White,
            start = Offset(22f * sx, 8f * sy),
            end = Offset(22f * sx, 20f * sy),
            strokeWidth = 4f * sx,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White,
            start = Offset(42f * sx, 8f * sy),
            end = Offset(42f * sx, 20f * sy),
            strokeWidth = 4f * sx,
            cap = StrokeCap.Round
        )
        val r = 3.5f * sx
        listOf(24f, 32f, 40f).forEach { cx ->
            drawCircle(Color.White, radius = r, center = Offset(cx * sx, 38f * sy))
        }
        listOf(24f, 32f).forEach { cx ->
            drawCircle(Color.White, radius = r, center = Offset(cx * sx, 46f * sy))
        }
    }
}

@Composable
fun NewsVectorIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(42.dp)) {
        val sx = size.width / 64f
        val sy = size.height / 64f
        val stroke = Stroke(width = 4f * sx, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(12f * sx, 12f * sy),
            size = Size(40f * sx, 40f * sy),
            cornerRadius = CornerRadius(4f * sx, 4f * sy),
            style = stroke
        )
        drawLine(
            color = Color.White,
            start = Offset(22f * sx, 22f * sy),
            end = Offset(42f * sx, 22f * sy),
            strokeWidth = 4f * sx,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White,
            start = Offset(22f * sx, 32f * sy),
            end = Offset(42f * sx, 32f * sy),
            strokeWidth = 4f * sx,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White,
            start = Offset(22f * sx, 42f * sy),
            end = Offset(34f * sx, 42f * sy),
            strokeWidth = 4f * sx,
            cap = StrokeCap.Round
        )
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(38f * sx, 38f * sy),
            size = Size(8f * sx, 8f * sy),
            cornerRadius = CornerRadius(2f * sx, 2f * sy)
        )
    }
}

@Composable
fun ClapperboardVectorIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(42.dp)) {
        val sx = size.width / 64f
        val sy = size.height / 64f
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(10f * sx, 24f * sy),
            size = Size(44f * sx, 28f * sy),
            cornerRadius = CornerRadius(4f * sx, 4f * sy)
        )
        listOf(10f, 22f, 34f, 46f).forEach { x ->
            val p = Path().apply {
                moveTo(x * sx, 24f * sy)
                lineTo((x + 8f) * sx, 14f * sy)
                lineTo((x + 12f) * sx, 16f * sy)
                lineTo((x + 4f) * sx, 26f * sy)
                close()
            }
            drawPath(p, color = Color.White)
        }
        val play = Path().apply {
            moveTo(28f * sx, 34f * sy)
            lineTo(38f * sx, 39f * sy)
            lineTo(28f * sx, 44f * sy)
            close()
        }
        drawPath(play, color = Color(0xFF0055CC))
    }
}
