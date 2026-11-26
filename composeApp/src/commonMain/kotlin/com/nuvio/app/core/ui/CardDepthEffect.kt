package com.nuvio.app.core.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun rememberCardDepthStyleUiState(): CardDepthStyleUiState {
    CardDepthStyleRepository.ensureLoaded()
    val uiState by CardDepthStyleRepository.uiState.collectAsState()
    return uiState
}

@Composable
fun Modifier.nuvioCardDepth(
    shape: Shape,
    surface: NuvioCardDepthSurface,
    fallbackBorderAlpha: Float = 0f,
): Modifier {
    val state = rememberCardDepthStyleUiState()
    val isLowEnd = LocalLowEndPerformanceMode.current
    val glowHover = this.nuvioBlazingRimGlow(cornerRadiusDp = 12f)

    if (isLowEnd || !state.isEnabledFor(surface)) {
        return glowHover
    }

    val baseSheen = state.sheenStrength.toFloat()
    return glowHover.cardDepthVisual(
        shape = shape,
        edgeStrength = 0f,
        sheenStrength = baseSheen,
        edgeCoverage = state.edgeCoverage.toFloat(),
    )
}

@Composable
fun Modifier.nuvioBlazingRimGlow(
    cornerRadiusDp: Float = 12f,
): Modifier {
    var isHovered by remember { mutableStateOf(false) }

    val hoverProgress by animateFloatAsState(
        targetValue = if (isHovered) 1f else 0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "blazingRimHover",
    )

    val phase = if (hoverProgress > 0.01f) {
        val infiniteTransition = rememberInfiniteTransition(label = "blazingRimFlow")
        val animatedPhase by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1600, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "blazingPhase",
        )
        animatedPhase
    } else {
        0f
    }

    val pointerHoverModifier = this
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    when (event.type) {
                        PointerEventType.Enter -> isHovered = true
                        PointerEventType.Exit -> isHovered = false
                    }
                }
            }
        }
        .graphicsLayer {
            val scale = 1f + 0.036f * hoverProgress
            scaleX = scale
            scaleY = scale
        }

    if (hoverProgress <= 0.01f) return pointerHoverModifier

    return pointerHoverModifier.drawWithContent {
        drawContent()

        val cornerPx = cornerRadiusDp.dp.toPx()
        val rad = phase * 2.0 * PI
        val cx = size.width / 2f
        val cy = size.height / 2f
        val rx = size.width * 0.7f
        val ry = size.height * 0.7f

        val startPt = Offset(
            x = cx + (cos(rad) * rx).toFloat(),
            y = cy + (sin(rad) * ry).toFloat(),
        )
        val endPt = Offset(
            x = cx - (cos(rad) * rx).toFloat(),
            y = cy - (sin(rad) * ry).toFloat(),
        )

        // Luong lua sang ruc (Trang choi + Xanh dien quang + Tim neon) chay doc 4 canh
        val fireBrush = Brush.linearGradient(
            colorStops = arrayOf(
                0.00f to Color.White.copy(alpha = 1.0f * hoverProgress),
                0.18f to Color(0xFF00F0FF).copy(alpha = 0.95f * hoverProgress),
                0.45f to Color(0xFF7C3AED).copy(alpha = 0.55f * hoverProgress),
                0.55f to Color.White.copy(alpha = 1.0f * hoverProgress),
                0.78f to Color(0xFF38BDF8).copy(alpha = 0.95f * hoverProgress),
                1.00f to Color.White.copy(alpha = 1.0f * hoverProgress),
            ),
            start = startPt,
            end = endPt,
        )

        // 1. Lop hao quang toa nhe sat mep vien (khong xoay khung hinh, giu nguyen poster ben trong)
        val auraStroke = 8.dp.toPx() * hoverProgress
        val auraInset = auraStroke / 2f
        drawRoundRect(
            brush = fireBrush,
            topLeft = Offset(auraInset, auraInset),
            size = Size(size.width - auraStroke, size.height - auraStroke),
            cornerRadius = CornerRadius((cornerPx - auraInset).coerceAtLeast(0f)),
            alpha = 0.35f * hoverProgress,
            style = Stroke(width = auraStroke),
        )

        // 2. Vien sang ruc sac net (3.6dp) om khit mep poster
        val mainStroke = 3.6.dp.toPx() * hoverProgress
        val mainInset = mainStroke / 2f
        drawRoundRect(
            brush = fireBrush,
            topLeft = Offset(mainInset, mainInset),
            size = Size(size.width - mainStroke, size.height - mainStroke),
            cornerRadius = CornerRadius((cornerPx - mainInset).coerceAtLeast(0f)),
            alpha = 1.0f * hoverProgress,
            style = Stroke(width = mainStroke),
        )

        // 3. Soi loi trang tinh khiet sang choi o mep ngoai cung
        val coreStroke = 1.3.dp.toPx() * hoverProgress
        val coreInset = coreStroke / 2f
        drawRoundRect(
            color = Color.White.copy(alpha = 0.92f * hoverProgress),
            topLeft = Offset(coreInset, coreInset),
            size = Size(size.width - coreStroke, size.height - coreStroke),
            cornerRadius = CornerRadius((cornerPx - coreInset).coerceAtLeast(0f)),
            style = Stroke(width = coreStroke),
        )
    }
}

fun Modifier.cardDepthVisual(
    shape: Shape,
    edgeStrength: Float,
    sheenStrength: Float,
    edgeCoverage: Float = DefaultCardDepthEdgeCoverage.toFloat(),
): Modifier {
    val edgeTop = edgeStrength.coerceIn(0f, 100f) / 100f
    val sheen = sheenStrength.coerceIn(0f, 100f) / 100f
    val coverage = edgeCoverage.coerceIn(0f, 100f) / 100f

    val withEdge = if (edgeTop > 0f) {
        border(
            width = 1.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = edgeTop),
                    Color.White.copy(alpha = edgeTop * (0.33f + 0.67f * coverage)),
                    Color.White.copy(alpha = edgeTop * coverage),
                ),
            ),
            shape = shape,
        )
    } else {
        this
    }

    return if (sheen > 0f) {
        withEdge.drawWithContent {
            drawContent()
            val sheenHeight = size.height * 0.22f
            if (sheenHeight > 0f) {
                val outline = shape.createOutline(size, layoutDirection, this)
                val shapePath = outline.toPath()
                clipPath(shapePath) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = sheen),
                                Color.Transparent,
                            ),
                            startY = 0f,
                            endY = sheenHeight,
                        ),
                        size = Size(size.width, sheenHeight),
                    )
                }
            }
        }
    } else {
        withEdge
    }
}

private fun Outline.toPath(): Path = when (this) {
    is Outline.Rectangle -> Path().apply { addRect(rect) }
    is Outline.Rounded -> Path().apply { addRoundRect(roundRect) }
    is Outline.Generic -> path
}
