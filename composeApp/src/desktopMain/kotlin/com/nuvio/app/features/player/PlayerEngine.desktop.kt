package com.nuvio.app.features.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.nuvio.app.DesktopFullscreenManager
import com.nuvio.app.features.streams.StreamSubtitle
import com.sun.jna.*
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.LongByReference
import com.sun.jna.ptr.PointerByReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import java.util.concurrent.atomic.AtomicBoolean

private const val MPV_RENDER_PARAM_API_TYPE = 1
private const val MPV_RENDER_PARAM_SW_SIZE = 17
private const val MPV_RENDER_PARAM_SW_FORMAT = 18
private const val MPV_RENDER_PARAM_SW_STRIDE = 19
private const val MPV_RENDER_PARAM_SW_POINTER = 20

private interface LibMpv : Library {
    fun mpv_create(): Pointer?
    fun mpv_initialize(ctx: Pointer): Int
    fun mpv_command(ctx: Pointer, args: Array<String?>): Int
    fun mpv_set_option_string(ctx: Pointer, name: String, data: String): Int
    fun mpv_set_property_string(ctx: Pointer, name: String, data: String): Int
    fun mpv_get_property_string(ctx: Pointer, name: String): String?
    fun mpv_get_property(ctx: Pointer, name: String, format: Int, data: Pointer): Int
    fun mpv_wait_event(ctx: Pointer, timeout: Double): MpvEvent?
    fun mpv_terminate_destroy(ctx: Pointer)

    fun mpv_render_context_create(res: PointerByReference, mpv: Pointer, params: Pointer): Int
    fun mpv_render_context_set_update_callback(ctx: Pointer, callback: MpvRenderUpdateFn?, callbackCtx: Pointer?)
    fun mpv_render_context_update(ctx: Pointer): Long
    fun mpv_render_context_render(ctx: Pointer, params: Pointer): Int
    fun mpv_render_context_free(ctx: Pointer)
}

fun interface MpvRenderUpdateFn : Callback {
    fun invoke(cbCtx: Pointer?)
}

@Structure.FieldOrder("event_id", "error", "reply_userdata", "data")
open class MpvEvent : Structure() {
    @JvmField var event_id: Int = 0
    @JvmField var error: Int = 0
    @JvmField var reply_userdata: Long = 0L
    @JvmField var data: Pointer? = null
}

private val mpvLib: LibMpv? by lazy {
    runCatching { Native.load("libmpv-2", LibMpv::class.java) }.getOrNull()
}

private fun writeParam(mem: Memory, index: Int, type: Int, data: Pointer?) {
    val offset = index * 16L
    mem.setInt(offset, type)
    mem.setInt(offset + 4L, 0)
    mem.setPointer(offset + 8L, data)
}

@Composable
actual fun PlatformPlayerSurface(
    sourceUrl: String,
    sourceAudioUrl: String?,
    sourceHeaders: Map<String, String>,
    sourceResponseHeaders: Map<String, String>,
    externalSubtitles: List<StreamSubtitle>,
    streamType: String?,
    useYoutubeChunkedPlayback: Boolean,
    drmType: String?,
    drmKey: String?,
    modifier: Modifier,
    playWhenReady: Boolean,
    initialPositionMs: Long?,
    initialPositionRequestKey: String?,
    resizeMode: PlayerResizeMode,
    playbackEngine: AndroidPlaybackEngine?,
    useNativeController: Boolean,
    onInitialPositionHandled: (String, Boolean) -> Unit,
    onControllerReady: (PlayerEngineController) -> Unit,
    onSnapshot: (PlayerPlaybackSnapshot) -> Unit,
    onError: (String?) -> Unit
) {
    var mpvHandle by remember { mutableStateOf<Pointer?>(null) }
    var renderCtx by remember { mutableStateOf<Pointer?>(null) }
    var viewportSize by remember { mutableStateOf(IntSize(1280, 720)) }
    var currentBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val frameDirty = remember { AtomicBoolean(false) }

    val updateCallback = remember {
        MpvRenderUpdateFn { _ ->
            frameDirty.set(true)
        }
    }

    DisposableEffect(Unit) {
        val lib = mpvLib
        if (lib == null) {
            onError("Khong tai duoc libmpv-2.dll tai D:/Tools/nuvio-deps/mpv")
            return@DisposableEffect onDispose {}
        }

        val ctx = lib.mpv_create()
        if (ctx == null) {
            onError("Khoi tao mpv_create that bai")
            return@DisposableEffect onDispose {}
        }

        lib.mpv_set_option_string(ctx, "vo", "libmpv")
        lib.mpv_set_option_string(ctx, "hwdec", "auto-safe")
        lib.mpv_set_option_string(ctx, "keep-open", "yes")
        lib.mpv_set_option_string(ctx, "ytdl", "no")
        lib.mpv_set_option_string(ctx, "msg-level", "cplayer=warn")
        lib.mpv_initialize(ctx)

        val apiTypeMem = Memory(4).apply { setString(0, "sw") }
        val paramsMem = Memory(2 * 16L)
        writeParam(paramsMem, 0, MPV_RENDER_PARAM_API_TYPE, apiTypeMem)
        writeParam(paramsMem, 1, 0, null)

        val rCtxRef = PointerByReference()
        val rRes = lib.mpv_render_context_create(rCtxRef, ctx, paramsMem)
        val createdRenderCtx = if (rRes >= 0) rCtxRef.value else null
        if (createdRenderCtx != null) {
            lib.mpv_render_context_set_update_callback(createdRenderCtx, updateCallback, null)
        } else {
            onError("Khoi tao mpv_render_context that bai (ma loi: $rRes)")
        }

        DesktopPlayerUiBridge.isFullscreenProvider = { DesktopFullscreenManager.isFullscreen.value }
        DesktopPlayerUiBridge.onToggleFullscreen = { DesktopFullscreenManager.toggle() }
        DesktopPlayerUiBridge.onVolumeChanged = { vol, muted ->
            lib.mpv_set_property_string(ctx, "volume", (vol * 100f).toInt().toString())
            lib.mpv_set_property_string(ctx, "mute", if (muted) "yes" else "no")
        }

        mpvHandle = ctx
        renderCtx = createdRenderCtx

        onDispose {
            DesktopPlayerUiBridge.onVolumeChanged = null
            createdRenderCtx?.let { r ->
                lib.mpv_render_context_set_update_callback(r, null, null)
                lib.mpv_render_context_free(r)
            }
            lib.mpv_terminate_destroy(ctx)
            renderCtx = null
            mpvHandle = null
        }
    }

    LaunchedEffect(renderCtx) {
        val lib = mpvLib ?: return@LaunchedEffect
        val rCtx = renderCtx ?: return@LaunchedEffect

        withContext(Dispatchers.Default) {
            var allocW = 0
            var allocH = 0
            var pixelMem: Memory? = null
            var byteBuffer = ByteArray(0)
            val sizeMem = Memory(8)
            val strideMem = IntByReference()
            val fmtMem = Memory(8).apply { setString(0, "bgra") }
            val renderParamsMem = Memory(5 * 16L)

            while (isActive) {
                if (!frameDirty.getAndSet(false)) {
                    delay(8)
                    continue
                }

                val flags = lib.mpv_render_context_update(rCtx)
                if ((flags and 1L) == 0L) continue

                val w = viewportSize.width.coerceAtLeast(2)
                val h = viewportSize.height.coerceAtLeast(2)
                val stride = w * 4
                val neededBytes = stride * h

                if (w != allocW || h != allocH || pixelMem == null) {
                    allocW = w
                    allocH = h
                    pixelMem = Memory(neededBytes.toLong())
                    byteBuffer = ByteArray(neededBytes)
                }

                sizeMem.setInt(0, w)
                sizeMem.setInt(4, h)
                strideMem.value = stride

                writeParam(renderParamsMem, 0, MPV_RENDER_PARAM_SW_SIZE, sizeMem)
                writeParam(renderParamsMem, 1, MPV_RENDER_PARAM_SW_FORMAT, fmtMem)
                writeParam(renderParamsMem, 2, MPV_RENDER_PARAM_SW_STRIDE, strideMem.pointer)
                writeParam(renderParamsMem, 3, MPV_RENDER_PARAM_SW_POINTER, pixelMem)
                writeParam(renderParamsMem, 4, 0, null)

                if (lib.mpv_render_context_render(rCtx, renderParamsMem) >= 0) {
                    pixelMem!!.read(0, byteBuffer, 0, neededBytes)
                    val imgInfo = ImageInfo(w, h, ColorType.BGRA_8888, ColorAlphaType.OPAQUE)
                    val skiaImg = Image.makeRaster(imgInfo, byteBuffer, stride)
                    val bmp = skiaImg.toComposeImageBitmap()
                    withContext(Dispatchers.Main) {
                        currentBitmap = bmp
                    }
                }
            }
        }
    }

    LaunchedEffect(mpvHandle) {
        val lib = mpvLib ?: return@LaunchedEffect
        val ctx = mpvHandle ?: return@LaunchedEffect
        onControllerReady(
            object : PlayerEngineController {
                override fun play() { lib.mpv_set_property_string(ctx, "pause", "no") }
                override fun pause() { lib.mpv_set_property_string(ctx, "pause", "yes") }
                override fun seekTo(positionMs: Long) {
                    lib.mpv_command(ctx, arrayOf("seek", (positionMs.coerceAtLeast(0L) / 1000.0).toString(), "absolute", null))
                }
                override fun seekBy(offsetMs: Long) {
                    lib.mpv_command(ctx, arrayOf("seek", (offsetMs / 1000.0).toString(), "relative", null))
                }
                override fun setPlaybackSpeed(speed: Float) {
                    lib.mpv_set_property_string(ctx, "speed", speed.toString())
                }
                override fun retry() {
                    if (sourceUrl.isNotBlank()) {
                        lib.mpv_command(ctx, arrayOf("loadfile", sourceUrl, "replace", null))
                    }
                }
                override fun getAudioTracks(): List<AudioTrack> = emptyList()
                override fun getSubtitleTracks(): List<SubtitleTrack> = emptyList()
                override fun applyAudioLanguagePreferences(languages: List<String>) {
                    if (languages.isNotEmpty()) lib.mpv_set_property_string(ctx, "alang", languages.joinToString(","))
                }
                override fun selectAudioTrack(index: Int) {
                    if (index >= 0) lib.mpv_set_property_string(ctx, "aid", (index + 1).toString())
                }
                override fun selectSubtitleTrack(index: Int) {
                    if (index >= 0) lib.mpv_set_property_string(ctx, "sid", (index + 1).toString())
                    else lib.mpv_set_property_string(ctx, "sid", "no")
                }
                override fun setSubtitleUri(url: String) {
                    if (url.isNotBlank()) lib.mpv_command(ctx, arrayOf("sub-add", url, "select", null))
                }
                override fun clearExternalSubtitle() {
                    lib.mpv_command(ctx, arrayOf("sub-remove", null))
                }
                override fun clearExternalSubtitleAndSelect(trackIndex: Int) {
                    lib.mpv_command(ctx, arrayOf("sub-remove", null))
                    selectSubtitleTrack(trackIndex)
                }
            }
        )
    }

    LaunchedEffect(mpvHandle, sourceUrl) {
        val lib = mpvLib ?: return@LaunchedEffect
        val ctx = mpvHandle ?: return@LaunchedEffect
        if (sourceUrl.isBlank()) return@LaunchedEffect

        onError(null)
        sourceHeaders["User-Agent"]?.let { lib.mpv_set_property_string(ctx, "user-agent", it) }
        val extraHeaders = sourceHeaders.filterKeys { !it.equals("User-Agent", true) }
            .entries.joinToString(",") { "${it.key}: ${it.value}" }
        if (extraHeaders.isNotEmpty()) {
            lib.mpv_set_property_string(ctx, "http-header-fields", extraHeaders)
        }

        lib.mpv_command(ctx, arrayOf("loadfile", sourceUrl, "replace", null))
        if (!playWhenReady) {
            lib.mpv_set_property_string(ctx, "pause", "yes")
        }
        if (initialPositionMs != null && initialPositionMs > 0L) {
            delay(400)
            lib.mpv_command(ctx, arrayOf("seek", (initialPositionMs / 1000.0).toString(), "absolute", null))
            initialPositionRequestKey?.let { onInitialPositionHandled(it, true) }
        }
        externalSubtitles.forEach { sub ->
            lib.mpv_command(ctx, arrayOf("sub-add", sub.url, "auto", sub.language ?: "und", null))
        }
    }

    LaunchedEffect(mpvHandle, playWhenReady) {
        val lib = mpvLib ?: return@LaunchedEffect
        val ctx = mpvHandle ?: return@LaunchedEffect
        lib.mpv_set_property_string(ctx, "pause", if (playWhenReady) "no" else "yes")
    }

    LaunchedEffect(mpvHandle) {
        val lib = mpvLib ?: return@LaunchedEffect
        val ctx = mpvHandle ?: return@LaunchedEffect
        val posRef = LongByReference()
        val durRef = LongByReference()
        while (isActive) {
            while (true) {
                val ev = lib.mpv_wait_event(ctx, 0.0) ?: break
                if (ev.event_id == 0) break
                if (ev.event_id == 7 && ev.error < 0) {
                    onError("Khong mo duoc nguon phat (ma loi mpv: ${ev.error})")
                }
            }
            delay(250)
            val posOk = lib.mpv_get_property(ctx, "time-pos", 4, posRef.pointer) == 0
            val durOk = lib.mpv_get_property(ctx, "duration", 4, durRef.pointer) == 0
            val paused = lib.mpv_get_property_string(ctx, "pause") == "yes"
            val coreIdle = lib.mpv_get_property_string(ctx, "core-idle") == "yes"
            val hasFrame = currentBitmap != null
            onSnapshot(
                PlayerPlaybackSnapshot(
                    isPlaying = !paused && hasFrame,
                    isLoading = !hasFrame && coreIdle,
                    positionMs = if (posOk) posRef.value * 1000L else 0L,
                    durationMs = if (durOk) durRef.value * 1000L else 0L
                )
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged { newSize ->
                if (newSize.width > 0 && newSize.height > 0) {
                    viewportSize = newSize
                    frameDirty.set(true)
                }
            }
    ) {
        val bmp = currentBitmap
        if (bmp != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawImage(
                    image = bmp,
                    dstOffset = IntOffset.Zero,
                    dstSize = IntSize(size.width.toInt(), size.height.toInt())
                )
            }
        }
    }
}
