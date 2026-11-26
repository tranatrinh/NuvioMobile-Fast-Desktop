package com.nuvio.app.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.ic_player_aspect_ratio
import nuvio.composeapp.generated.resources.ic_player_audio_filled
import nuvio.composeapp.generated.resources.ic_player_episodes
import nuvio.composeapp.generated.resources.ic_player_pause
import nuvio.composeapp.generated.resources.ic_player_play
import nuvio.composeapp.generated.resources.ic_player_source
import nuvio.composeapp.generated.resources.ic_player_subtitles
import nuvio.composeapp.generated.resources.library_add_plus
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.Image
import coil3.ImageLoader
import coil3.toBitmap
import com.nuvio.app.DesktopPrefs
import com.nuvio.app.DesktopFullscreenManager
import com.nuvio.app.features.settings.NavBarStyle
import dev.chrisbanes.haze.HazeState
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo

@Composable
actual fun appIconPainter(icon: AppIconResource): Painter =
    painterResource(
        when (icon) {
            AppIconResource.PlayerPlay -> Res.drawable.ic_player_play
            AppIconResource.PlayerPause -> Res.drawable.ic_player_pause
            AppIconResource.PlayerAspectRatio -> Res.drawable.ic_player_aspect_ratio
            AppIconResource.PlayerSubtitles -> Res.drawable.ic_player_subtitles
            AppIconResource.PlayerAudioFilled -> Res.drawable.ic_player_audio_filled
            AppIconResource.PlayerSource -> Res.drawable.ic_player_source
            AppIconResource.PlayerEpisodes -> Res.drawable.ic_player_episodes
            AppIconResource.LibraryAddPlus -> Res.drawable.library_add_plus
            else -> Res.drawable.ic_player_play
        }
    )

private fun Bitmap.toArgbIntArray(): IntArray {
    val info = ImageInfo(width, height, ColorType.BGRA_8888, ColorAlphaType.UNPREMUL)
    val bytes = readPixels(info, width * 4, 0, 0) ?: return IntArray(width * height)
    val ints = IntArray(width * height)
    for (i in ints.indices) {
        val b = bytes[i * 4].toInt() and 0xFF
        val g = bytes[i * 4 + 1].toInt() and 0xFF
        val r = bytes[i * 4 + 2].toInt() and 0xFF
        val a = bytes[i * 4 + 3].toInt() and 0xFF
        ints[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
    }
    return ints
}

internal actual fun prepareArtworkBackdrop(image: coil3.Image): ImageBitmap {
    val bmp = image.toBitmap()
    val w = bmp.width
    val h = bmp.height
    val pixels = bmp.toArgbIntArray()
    blurBackdropPixels(pixels, w, h)
    val bytes = ByteArray(w * h * 4)
    for (i in pixels.indices) {
        val c = pixels[i]
        bytes[i * 4] = (c and 0xFF).toByte()
        bytes[i * 4 + 1] = ((c shr 8) and 0xFF).toByte()
        bytes[i * 4 + 2] = ((c shr 16) and 0xFF).toByte()
        bytes[i * 4 + 3] = ((c shr 24) and 0xFF).toByte()
    }
    val out = Bitmap()
    out.allocN32Pixels(w, h)
    out.installPixels(bytes)
    return out.asComposeImageBitmap()
}

internal actual val ashSwarmMaxGrainBudget: Int = 120

internal actual class AshSwarmRenderer actual constructor(private val maxGrains: Int) {
    actual fun draw(
        scope: DrawScope,
        count: Int,
        centersX: FloatArray,
        centersY: FloatArray,
        halfWidths: FloatArray,
        halfHeights: FloatArray,
        colors: IntArray,
    ) {
        val n = minOf(count, maxGrains)
        for (i in 0 until n) {
            scope.drawRect(
                color = Color(colors[i]),
                topLeft = Offset(centersX[i] - halfWidths[i], centersY[i] - halfHeights[i]),
                size = Size(halfWidths[i] * 2f, halfHeights[i] * 2f)
            )
        }
    }
}

internal actual fun sampleBackdropPixels(image: Image, targetWidth: Int): SampledPixels? {
    val bmp = image.toBitmap()
    if (bmp.width <= 0 || bmp.height <= 0) return null
    return downsampleArgb(bmp.toArgbIntArray(), bmp.width, bmp.height, targetWidth)
}

internal actual object CardDepthStyleStorage {
    actual fun loadPayload(): String? = DesktopPrefs.getString("card_depth_style")
    actual fun savePayload(payload: String) = DesktopPrefs.putString("card_depth_style", payload)
}

internal actual object PosterCardStyleStorage {
    actual fun loadPayload(): String? = DesktopPrefs.getString("poster_card_style")
    actual fun savePayload(payload: String) = DesktopPrefs.putString("poster_card_style", payload)
}

internal actual val floatingNavigationGlowSupported: Boolean = true

@Composable
internal actual fun FloatingNavigationBar(
    items: List<FloatingNavigationItem>,
    modifier: Modifier,
    scrollState: NuvioNavBarScrollState?,
    hazeState: HazeState?,
    contentPadding: PaddingValues,
    compactSize: Boolean,
    glowEnabled: Boolean,
) {
    NuvioNavigationBar(
        modifier = modifier,
        scrollState = scrollState,
        hazeState = hazeState,
        contentPadding = contentPadding,
        compactSize = compactSize,
    ) {
        items.forEach { item ->
            when {
                item.content != null -> NavItem(
                    selected = item.selected,
                    onClick = item.onClick,
                    label = item.label,
                ) {
                    item.content.invoke(item.onClick)
                }
                item.icon != null -> NavItem(
                    selected = item.selected,
                    onClick = item.onClick,
                    icon = item.icon,
                    contentDescription = item.label,
                    label = item.label,
                )
                item.drawable != null -> NavItem(
                    selected = item.selected,
                    onClick = item.onClick,
                    icon = item.drawable,
                    contentDescription = item.label,
                    label = item.label,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun NuvioNativeModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    containerColor: Color,
    contentColor: Color,
    showDragHandle: Boolean,
    fullHeight: Boolean,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        content = content
    )
}

internal actual val usesNativeNuvioBottomSheet: Boolean = false
internal actual fun dismissNativeNuvioBottomSheet() {}

internal actual fun isLiquidGlassNativeTabBarSupported(): Boolean = false
internal actual fun publishLiquidGlassNativeTabBarEnabled(enabled: Boolean) {}
internal actual fun publishNativeTabBarVisible(visible: Boolean) {}
internal actual fun publishNativeSelectedTab(tabName: String) {}
internal actual fun publishNativeTabAccentColor(hexColor: String) {}
internal actual fun publishNativeTabTitles(home: String, search: String, library: String, profile: String) {}
internal actual fun publishNativeProfileTabIcon(name: String?, avatarColorHex: String?, avatarImageUrl: String?, avatarBackgroundColorHex: String?) {}

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {}
actual fun platformExitApp() { kotlin.system.exitProcess(0) }

internal actual fun ImageLoader.Builder.configurePlatformImageLoader(): ImageLoader.Builder = this
internal actual val platformProvidesImageLoader: Boolean = false

internal actual val nuvioPlatformExtraTopPadding: Dp = 0.dp
internal actual val nuvioPlatformExtraBottomPadding: Dp = 0.dp
internal actual val nuvioBottomNavigationExtraVerticalPadding: Dp = 0.dp
@Composable internal actual fun nuvioBottomNavigationBarInsets(): WindowInsets = WindowInsets(0, 0, 0, 0)
@Composable internal actual fun platformPhysicalTopInset(): Dp = 0.dp
@Composable internal actual fun ResizeForKeyboard() {}





