package com.nuvio.app.features.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntSize
import com.nuvio.app.DesktopPrefs
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.net.URI
import java.util.Calendar
import java.util.Locale



@Composable
actual fun rememberExternalPlayerLauncher(
    onResult: (ExternalPlaybackResult?) -> Unit,
): (ExternalPlayerIntentResult.Success) -> Boolean = { false }

internal actual object ExternalPlayerPlatform {
    actual fun defaultPlayerId(): String? = "system_browser"
    actual fun availablePlayers(): List<ExternalPlayerApp> = emptyList()
    actual fun open(request: ExternalPlayerPlaybackRequest, playerId: String?): ExternalPlayerOpenResult {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(URI(request.sourceUrl))
        }
        return ExternalPlayerOpenResult.Opened
    }
    actual fun buildIntent(request: ExternalPlayerPlaybackRequest, playerId: String?): ExternalPlayerIntentResult =
        ExternalPlayerIntentResult.Failed
}

internal actual fun applyExoPlayerNativeMemory(enabled: Boolean) {}
internal actual fun isExoNativeMemorySupported(): Boolean = false
internal actual fun exoNativeMemoryInfo(): ExoNativeMemoryInfo? = null
internal actual fun playbackCacheUsableSpaceBytes(): Long = File(".").usableSpace
internal actual fun playbackHeapBufferMaxMb(): Int = 256

@Composable
actual fun rememberFontPickerLauncher(onFontSelected: (name: String, path: String) -> Unit): () -> Unit = {
    val fd = FileDialog(null as Frame?, "Chon Font", FileDialog.LOAD)
    fd.isVisible = true
    if (fd.file != null) onFontSelected(fd.file, File(fd.directory ?: "", fd.file).absolutePath)
}

@Composable
actual fun rememberSubtitleFilePickerLauncher(onSubtitleSelected: (name: String, fileUri: String) -> Unit): () -> Unit = {
    val fd = FileDialog(null as Frame?, "Chon Phu De", FileDialog.LOAD)
    fd.isVisible = true
    if (fd.file != null) onSubtitleSelected(fd.file, File(fd.directory ?: "", fd.file).toURI().toString())
}

internal actual object DeviceLanguagePreferences {
    actual fun preferredLanguageCodes(): List<String> = listOf(Locale.getDefault().language, "vi", "en").distinct()
}

@Composable actual fun LockPlayerToLandscape() {}
@Composable actual fun ApplyPlayerOrientation(isPortrait: Boolean) {}
@Composable actual fun FullscreenPlayerDialog(onDismiss: () -> Unit, content: @Composable () -> Unit) { content() }
@Composable actual fun HidePlayerSystemBars() {}
@Composable actual fun EnterImmersivePlayerMode(keepScreenAwake: Boolean) {}
@Composable actual fun ManagePlayerPictureInPicture(isPlaying: Boolean, videoSize: IntSize) {}
@Composable actual fun rememberIsInPictureInPicture(): Boolean = false
@Composable internal actual fun rememberAudioOutputKey(enabled: Boolean): String? = "windows_default"
@Composable internal actual fun rememberPlatformPlayerGestureController(): PlayerGestureController? = null

internal actual object PlayerTrackPreferenceStorage {
    actual fun load(contentId: String): PersistedPlayerTrackPreference? = null
    actual fun save(contentId: String, preference: PersistedPlayerTrackPreference) {}
    actual fun loadSubtitleDelayMs(videoId: String): Int? = DesktopPrefs.getInt("sub_delay_$videoId")
    actual fun saveSubtitleDelayMs(videoId: String, delayMs: Int) = DesktopPrefs.putInt("sub_delay_$videoId", delayMs)
    actual fun loadAudioDelayMs(audioOutputKey: String): Int? = DesktopPrefs.getInt("aud_delay_$audioOutputKey")
    actual fun saveAudioDelayMs(audioOutputKey: String, delayMs: Int) = DesktopPrefs.putInt("aud_delay_$audioOutputKey", delayMs)
}

actual object SubtitleCacheProvider {
    actual suspend fun cacheForExternalPlayer(subtitles: List<SubtitleInput>): List<SubtitleInput>? = subtitles
}

internal actual object SubtitleFontStorage {
    private val dir = File(System.getProperty("user.home"), ".nuvio/fonts").apply { mkdirs() }
    actual fun defaultFamily(): FontFamily = FontFamily.SansSerif
    actual fun read(name: String): ByteArray? = File(dir, name).takeIf { it.exists() }?.readBytes()
    actual fun write(name: String, bytes: ByteArray) { File(dir, name).writeBytes(bytes) }
    actual fun remove(name: String) { File(dir, name).delete() }
    actual fun fontFamily(id: String): FontFamily? = FontFamily.SansSerif
}

@Composable internal actual fun rememberSubtitleFontImporter(onResult: (Result<SubtitleFont>) -> Unit): () -> Unit = {}

internal actual suspend fun computeVideoHash(url: String, headers: Map<String, String>): Pair<String, Long>? = null



internal actual object PlayerSettingsStorage {
    actual fun loadPendingExternalPlayback(): String? = DesktopPrefs.getString("p_ext")
    actual fun savePendingExternalPlayback(value: String?) = DesktopPrefs.putString("p_ext", value)
    actual fun loadPlaybackBrightness(): Float? = DesktopPrefs.getFloat("p_bright")
    actual fun savePlaybackBrightness(level: Float) = DesktopPrefs.putFloat("p_bright", level)
    actual fun loadUseLegacyPlayerLayout(): Boolean? = DesktopPrefs.getBoolean("p_legacy")
    actual fun saveUseLegacyPlayerLayout(enabled: Boolean) = DesktopPrefs.putBoolean("p_legacy", enabled)
    actual fun loadShowLoadingOverlay(): Boolean? = DesktopPrefs.getBoolean("p_load_ov")
    actual fun saveShowLoadingOverlay(enabled: Boolean) = DesktopPrefs.putBoolean("p_load_ov", enabled)
    actual fun loadAutoSwitchPlaybackEngineOnError(): Boolean? = DesktopPrefs.getBoolean("p_autosw")
    actual fun saveAutoSwitchPlaybackEngineOnError(enabled: Boolean) = DesktopPrefs.putBoolean("p_autosw", enabled)
    actual fun loadStreamAutoPlayFallbackOnFailure(): Boolean? = DesktopPrefs.getBoolean("p_ap_fb")
    actual fun saveStreamAutoPlayFallbackOnFailure(enabled: Boolean) = DesktopPrefs.putBoolean("p_ap_fb", enabled)
    actual fun loadStreamAutoPlayFallbackMaxSources(): Int? = DesktopPrefs.getInt("p_ap_max")
    actual fun saveStreamAutoPlayFallbackMaxSources(count: Int) = DesktopPrefs.putInt("p_ap_max", count)
    actual fun loadStreamAutoPlayFallbackStartupTimeoutSeconds(): Int? = DesktopPrefs.getInt("p_ap_to")
    actual fun saveStreamAutoPlayFallbackStartupTimeoutSeconds(seconds: Int) = DesktopPrefs.putInt("p_ap_to", seconds)
    actual fun loadShowPlayerLoadingStatus(): Boolean? = DesktopPrefs.getBoolean("p_load_st")
    actual fun saveShowPlayerLoadingStatus(enabled: Boolean) = DesktopPrefs.putBoolean("p_load_st", enabled)
    actual fun loadPauseOverlayEnabled(): Boolean? = DesktopPrefs.getBoolean("p_pause_ov")
    actual fun savePauseOverlayEnabled(enabled: Boolean) = DesktopPrefs.putBoolean("p_pause_ov", enabled)
    actual fun loadShowParentalGuide(): Boolean? = DesktopPrefs.getBoolean("p_pg")
    actual fun saveShowParentalGuide(enabled: Boolean) = DesktopPrefs.putBoolean("p_pg", enabled)
    actual fun loadResizeMode(): String? = DesktopPrefs.getString("p_resize")
    actual fun saveResizeMode(mode: String) = DesktopPrefs.putString("p_resize", mode)
    actual fun loadHoldToSpeedEnabled(): Boolean? = DesktopPrefs.getBoolean("p_hold_en")
    actual fun saveHoldToSpeedEnabled(enabled: Boolean) = DesktopPrefs.putBoolean("p_hold_en", enabled)
    actual fun loadHoldToSpeedValue(): Float? = DesktopPrefs.getFloat("p_hold_val")
    actual fun saveHoldToSpeedValue(speed: Float) = DesktopPrefs.putFloat("p_hold_val", speed)
    actual fun loadTouchGesturesEnabled(): Boolean? = DesktopPrefs.getBoolean("p_gest")
    actual fun saveTouchGesturesEnabled(enabled: Boolean) = DesktopPrefs.putBoolean("p_gest", enabled)
    actual fun loadExternalPlayerEnabled(): Boolean? = DesktopPrefs.getBoolean("p_ext_en")
    actual fun saveExternalPlayerEnabled(enabled: Boolean) = DesktopPrefs.putBoolean("p_ext_en", enabled)
    actual fun loadExternalPlayerForwardSubtitles(): Boolean? = DesktopPrefs.getBoolean("p_ext_sub")
    actual fun saveExternalPlayerForwardSubtitles(enabled: Boolean) = DesktopPrefs.putBoolean("p_ext_sub", enabled)
    actual fun loadExternalPlayerSendSkipSegments(): Boolean? = DesktopPrefs.getBoolean("p_ext_skip")
    actual fun saveExternalPlayerSendSkipSegments(enabled: Boolean) = DesktopPrefs.putBoolean("p_ext_skip", enabled)
    actual fun loadExternalPlayerId(): String? = DesktopPrefs.getString("p_ext_id")
    actual fun saveExternalPlayerId(playerId: String?) = DesktopPrefs.putString("p_ext_id", playerId)
    actual fun loadPreferredAudioLanguage(): String? = DesktopPrefs.getString("p_aud_lang")
    actual fun savePreferredAudioLanguage(language: String) = DesktopPrefs.putString("p_aud_lang", language)
    actual fun loadSecondaryPreferredAudioLanguage(): String? = DesktopPrefs.getString("p_aud_lang2")
    actual fun saveSecondaryPreferredAudioLanguage(language: String?) = DesktopPrefs.putString("p_aud_lang2", language)
    actual fun loadPreferredSubtitleLanguage(): String? = DesktopPrefs.getString("p_sub_lang")
    actual fun savePreferredSubtitleLanguage(language: String) = DesktopPrefs.putString("p_sub_lang", language)
    actual fun loadSecondaryPreferredSubtitleLanguage(): String? = DesktopPrefs.getString("p_sub_lang2")
    actual fun saveSecondaryPreferredSubtitleLanguage(language: String?) = DesktopPrefs.putString("p_sub_lang2", language)
    actual fun loadSubtitleTextColor(): String? = DesktopPrefs.getString("p_sub_tc")
    actual fun saveSubtitleTextColor(colorHex: String) = DesktopPrefs.putString("p_sub_tc", colorHex)
    actual fun loadSubtitleBackgroundColor(): String? = DesktopPrefs.getString("p_sub_bg")
    actual fun saveSubtitleBackgroundColor(colorHex: String) = DesktopPrefs.putString("p_sub_bg", colorHex)
    actual fun loadSubtitleAppearance(): String? = DesktopPrefs.getString("p_sub_app")
    actual fun saveSubtitleAppearance(value: String) = DesktopPrefs.putString("p_sub_app", value)
    actual fun loadSubtitleOutlineColor(): String? = DesktopPrefs.getString("p_sub_oc")
    actual fun saveSubtitleOutlineColor(colorHex: String) = DesktopPrefs.putString("p_sub_oc", colorHex)
    actual fun loadSubtitleOutlineEnabled(): Boolean? = DesktopPrefs.getBoolean("p_sub_oe")
    actual fun saveSubtitleOutlineEnabled(enabled: Boolean) = DesktopPrefs.putBoolean("p_sub_oe", enabled)
    actual fun loadSubtitleOutlineWidth(): Int? = DesktopPrefs.getInt("p_sub_ow")
    actual fun saveSubtitleOutlineWidth(width: Int) = DesktopPrefs.putInt("p_sub_ow", width)
    actual fun loadSubtitleBold(): Boolean? = DesktopPrefs.getBoolean("p_sub_bold")
    actual fun saveSubtitleBold(enabled: Boolean) = DesktopPrefs.putBoolean("p_sub_bold", enabled)
    actual fun loadSubtitleFontSizeSp(): Int? = DesktopPrefs.getInt("p_sub_fs")
    actual fun saveSubtitleFontSizeSp(fontSizeSp: Int) = DesktopPrefs.putInt("p_sub_fs", fontSizeSp)
    actual fun loadSubtitleBottomOffset(): Int? = DesktopPrefs.getInt("p_sub_bo")
    actual fun saveSubtitleBottomOffset(bottomOffset: Int) = DesktopPrefs.putInt("p_sub_bo", bottomOffset)
    actual fun loadSubtitleStripSdh(): Boolean? = DesktopPrefs.getBoolean("p_sub_sdh")
    actual fun saveSubtitleStripSdh(enabled: Boolean) = DesktopPrefs.putBoolean("p_sub_sdh", enabled)
    actual fun loadSubtitleUseForcedSubtitles(): Boolean? = DesktopPrefs.getBoolean("p_sub_forced")
    actual fun saveSubtitleUseForcedSubtitles(enabled: Boolean) = DesktopPrefs.putBoolean("p_sub_forced", enabled)
    actual fun loadSubtitleShowOnlyPreferredLanguages(): Boolean? = DesktopPrefs.getBoolean("p_sub_only")
    actual fun saveSubtitleShowOnlyPreferredLanguages(enabled: Boolean) = DesktopPrefs.putBoolean("p_sub_only", enabled)
    actual fun loadSubtitleFontName(): String? = DesktopPrefs.getString("p_sub_fn")
    actual fun saveSubtitleFontName(fontName: String) = DesktopPrefs.putString("p_sub_fn", fontName)
    actual fun loadSubtitleCustomFontPath(): String? = DesktopPrefs.getString("p_sub_cfp")
    actual fun saveSubtitleCustomFontPath(path: String?) = DesktopPrefs.putString("p_sub_cfp", path)
    actual fun loadSubtitleCustomFontName(): String? = DesktopPrefs.getString("p_sub_cfn")
    actual fun saveSubtitleCustomFontName(name: String?) = DesktopPrefs.putString("p_sub_cfn", name)
    actual fun loadStreamReuseLastLinkEnabled(): Boolean? = DesktopPrefs.getBoolean("p_reuse")
    actual fun saveStreamReuseLastLinkEnabled(enabled: Boolean) = DesktopPrefs.putBoolean("p_reuse", enabled)
    actual fun loadStreamReuseLastLinkCacheHours(): Int? = DesktopPrefs.getInt("p_reuse_h")
    actual fun saveStreamReuseLastLinkCacheHours(hours: Int) = DesktopPrefs.putInt("p_reuse_h", hours)
    actual fun loadAndroidPlaybackEngine(): String? = DesktopPrefs.getString("p_eng")
    actual fun saveAndroidPlaybackEngine(engine: String) = DesktopPrefs.putString("p_eng", engine)
    actual fun loadAndroidLibmpvVideoOutput(): String? = DesktopPrefs.getString("p_mpv_vo")
    actual fun saveAndroidLibmpvVideoOutput(output: String) = DesktopPrefs.putString("p_mpv_vo", output)
    actual fun loadAndroidLibmpvHardwareDecodingEnabled(): Boolean? = DesktopPrefs.getBoolean("p_mpv_hw")
    actual fun saveAndroidLibmpvHardwareDecodingEnabled(enabled: Boolean) = DesktopPrefs.putBoolean("p_mpv_hw", enabled)
    actual fun loadAndroidLibmpvYuv420pEnabled(): Boolean? = DesktopPrefs.getBoolean("p_mpv_yuv")
    actual fun loadAndroidExoChaptersEnabled(): Boolean? = DesktopPrefs.getBoolean("p_exo_ch")
    actual fun loadChapterTitleEnabled(): Boolean? = DesktopPrefs.getBoolean("p_ch_t")
    actual fun saveAndroidLibmpvYuv420pEnabled(enabled: Boolean) = DesktopPrefs.putBoolean("p_mpv_yuv", enabled)
    actual fun saveAndroidExoChaptersEnabled(enabled: Boolean) = DesktopPrefs.putBoolean("p_exo_ch", enabled)
    actual fun saveChapterTitleEnabled(enabled: Boolean) = DesktopPrefs.putBoolean("p_ch_t", enabled)
    actual fun loadRememberAudioDelayPerDevice(): Boolean? = DesktopPrefs.getBoolean("p_aud_rem")
    actual fun saveRememberAudioDelayPerDevice(enabled: Boolean) = DesktopPrefs.putBoolean("p_aud_rem", enabled)
    actual fun loadPersistAudioAmplification(): Boolean? = DesktopPrefs.getBoolean("p_aud_amp_p")
    actual fun savePersistAudioAmplification(enabled: Boolean) = DesktopPrefs.putBoolean("p_aud_amp_p", enabled)
    actual fun loadAudioAmplificationDb(): Int? = DesktopPrefs.getInt("p_aud_amp")
    actual fun saveAudioAmplificationDb(db: Int) = DesktopPrefs.putInt("p_aud_amp", db)
    actual fun loadDownmixEnabled(): Boolean? = DesktopPrefs.getBoolean("p_dmx")
    actual fun saveDownmixEnabled(enabled: Boolean) = DesktopPrefs.putBoolean("p_dmx", enabled)
    actual fun loadAudioOutputChannels(): String? = DesktopPrefs.getString("p_aud_ch")
    actual fun saveAudioOutputChannels(value: String) = DesktopPrefs.putString("p_aud_ch", value)
    actual fun loadMaintainOriginalAudioOnDownmix(): Boolean? = DesktopPrefs.getBoolean("p_dmx_orig")
    actual fun saveMaintainOriginalAudioOnDownmix(enabled: Boolean) = DesktopPrefs.putBoolean("p_dmx_orig", enabled)
    actual fun loadCenterMixLevelDb(): Int? = DesktopPrefs.getInt("p_dmx_c")
    actual fun saveCenterMixLevelDb(db: Int) = DesktopPrefs.putInt("p_dmx_c", db)
    actual fun loadDecoderPriority(): Int? = DesktopPrefs.getInt("p_dec_p")
    actual fun saveDecoderPriority(priority: Int) = DesktopPrefs.putInt("p_dec_p", priority)
    actual fun loadMapDV7ToHevc(): Boolean? = DesktopPrefs.getBoolean("p_dv7")
    actual fun saveMapDV7ToHevc(enabled: Boolean) = DesktopPrefs.putBoolean("p_dv7", enabled)
    actual fun loadTunnelingEnabled(): Boolean? = DesktopPrefs.getBoolean("p_tun")
    actual fun saveTunnelingEnabled(enabled: Boolean) = DesktopPrefs.putBoolean("p_tun", enabled)
    actual fun loadExoNativeMemoryEnabled(): Boolean? = DesktopPrefs.getBoolean("p_exo_mem")
    actual fun saveExoNativeMemoryEnabled(enabled: Boolean) = DesktopPrefs.putBoolean("p_exo_mem", enabled)
    actual fun loadVodCacheEnabled(): Boolean? = DesktopPrefs.getBoolean("p_vod")
    actual fun saveVodCacheEnabled(enabled: Boolean) = DesktopPrefs.putBoolean("p_vod", enabled)
    actual fun loadVodCacheSizeMode(): String? = DesktopPrefs.getString("p_vod_m")
    actual fun saveVodCacheSizeMode(mode: String) = DesktopPrefs.putString("p_vod_m", mode)
    actual fun loadVodCacheSizeMb(): Int? = DesktopPrefs.getInt("p_vod_mb")
    actual fun saveVodCacheSizeMb(sizeMb: Int) = DesktopPrefs.putInt("p_vod_mb", sizeMb)
    actual fun loadBufferEngineEnabled(): Boolean? = DesktopPrefs.getBoolean("p_buf")
    actual fun saveBufferEngineEnabled(enabled: Boolean) = DesktopPrefs.putBoolean("p_buf", enabled)
    actual fun loadMinBufferMs(): Int? = DesktopPrefs.getInt("p_buf_min")
    actual fun saveMinBufferMs(value: Int) = DesktopPrefs.putInt("p_buf_min", value)
    actual fun loadMaxBufferMs(): Int? = DesktopPrefs.getInt("p_buf_max")
    actual fun saveMaxBufferMs(value: Int) = DesktopPrefs.putInt("p_buf_max", value)
    actual fun loadBufferForPlaybackMs(): Int? = DesktopPrefs.getInt("p_buf_pb")
    actual fun saveBufferForPlaybackMs(value: Int) = DesktopPrefs.putInt("p_buf_pb", value)
    actual fun loadBufferForPlaybackAfterRebufferMs(): Int? = DesktopPrefs.getInt("p_buf_rb")
    actual fun saveBufferForPlaybackAfterRebufferMs(value: Int) = DesktopPrefs.putInt("p_buf_rb", value)
    actual fun loadBackBufferDurationMs(): Int? = DesktopPrefs.getInt("p_buf_bk")
    actual fun saveBackBufferDurationMs(value: Int) = DesktopPrefs.putInt("p_buf_bk", value)
    actual fun loadTargetBufferSizeMb(): Int? = DesktopPrefs.getInt("p_buf_mb")
    actual fun saveTargetBufferSizeMb(value: Int) = DesktopPrefs.putInt("p_buf_mb", value)
    actual fun loadStreamAutoPlayMode(): String? = DesktopPrefs.getString("p_ap_mode")
    actual fun saveStreamAutoPlayMode(mode: String) = DesktopPrefs.putString("p_ap_mode", mode)
    actual fun loadStreamAutoPlaySource(): String? = DesktopPrefs.getString("p_ap_src")
    actual fun saveStreamAutoPlaySource(source: String) = DesktopPrefs.putString("p_ap_src", source)
    actual fun loadStreamAutoPlaySelectedAddons(): Set<String>? = DesktopPrefs.getStringSet("p_ap_add")
    actual fun saveStreamAutoPlaySelectedAddons(addons: Set<String>) = DesktopPrefs.putStringSet("p_ap_add", addons)
    actual fun loadStreamAutoPlaySelectedPlugins(): Set<String>? = DesktopPrefs.getStringSet("p_ap_plg")
    actual fun saveStreamAutoPlaySelectedPlugins(plugins: Set<String>) = DesktopPrefs.putStringSet("p_ap_plg", plugins)
    actual fun loadStreamAutoPlayRegex(): String? = DesktopPrefs.getString("p_ap_rx")
    actual fun saveStreamAutoPlayRegex(regex: String) = DesktopPrefs.putString("p_ap_rx", regex)
    actual fun loadStreamAutoPlayTimeoutSeconds(): Int? = DesktopPrefs.getInt("p_ap_t")
    actual fun saveStreamAutoPlayTimeoutSeconds(seconds: Int) = DesktopPrefs.putInt("p_ap_t", seconds)
    actual fun loadSkipIntroEnabled(): Boolean? = DesktopPrefs.getBoolean("p_skip_i")
    actual fun loadAutoSkipMovieCredits(): Boolean? = DesktopPrefs.getBoolean("p_skip_mc")
    actual fun loadAutoSkipSegmentTypes(): Set<String>? = DesktopPrefs.getStringSet("p_skip_seg")
    actual fun saveAutoSkipSegmentTypes(types: Set<String>) = DesktopPrefs.putStringSet("p_skip_seg", types)
    actual fun saveAutoSkipMovieCredits(enabled: Boolean) = DesktopPrefs.putBoolean("p_skip_mc", enabled)
    actual fun loadAutoSkipPostCredits(): Boolean? = DesktopPrefs.getBoolean("p_skip_pc")
    actual fun saveAutoSkipPostCredits(enabled: Boolean) = DesktopPrefs.putBoolean("p_skip_pc", enabled)
    actual fun saveSkipIntroEnabled(enabled: Boolean) = DesktopPrefs.putBoolean("p_skip_i", enabled)
    actual fun loadAnimeSkipEnabled(): Boolean? = DesktopPrefs.getBoolean("p_askip")
    actual fun saveAnimeSkipEnabled(enabled: Boolean) = DesktopPrefs.putBoolean("p_askip", enabled)
    actual fun loadAnimeSkipClientId(): String? = DesktopPrefs.getString("p_askip_id")
    actual fun saveAnimeSkipClientId(clientId: String) = DesktopPrefs.putString("p_askip_id", clientId)
    actual fun loadIntroDbApiKey(): String? = DesktopPrefs.getString("p_idb")
    actual fun saveIntroDbApiKey(apiKey: String) = DesktopPrefs.putString("p_idb", apiKey)
    actual fun loadIntroSubmitEnabled(): Boolean? = DesktopPrefs.getBoolean("p_idb_sub")
    actual fun saveIntroSubmitEnabled(enabled: Boolean) = DesktopPrefs.putBoolean("p_idb_sub", enabled)
    actual fun loadStreamAutoPlayNextEpisodeEnabled(): Boolean? = DesktopPrefs.getBoolean("p_ap_ne")
    actual fun saveStreamAutoPlayNextEpisodeEnabled(enabled: Boolean) = DesktopPrefs.putBoolean("p_ap_ne", enabled)
    actual fun loadStreamAutoPlayNextEpisodeFallbackEnabled(): Boolean? = DesktopPrefs.getBoolean("p_ap_ne_fb")
    actual fun saveStreamAutoPlayNextEpisodeFallbackEnabled(enabled: Boolean) = DesktopPrefs.putBoolean("p_ap_ne_fb", enabled)
    actual fun loadStreamAutoPlayPreferBingeGroup(): Boolean? = DesktopPrefs.getBoolean("p_ap_bg")
    actual fun saveStreamAutoPlayPreferBingeGroup(enabled: Boolean) = DesktopPrefs.putBoolean("p_ap_bg", enabled)
    actual fun loadStreamAutoPlayReuseBingeGroup(): Boolean? = DesktopPrefs.getBoolean("p_ap_bg_r")
    actual fun saveStreamAutoPlayReuseBingeGroup(enabled: Boolean) = DesktopPrefs.putBoolean("p_ap_bg_r", enabled)
    actual fun loadNextEpisodeThresholdMode(): String? = DesktopPrefs.getString("p_ne_m")
    actual fun saveNextEpisodeThresholdMode(mode: String) = DesktopPrefs.putString("p_ne_m", mode)
    actual fun loadNextEpisodeThresholdPercent(): Float? = DesktopPrefs.getFloat("p_ne_p")
    actual fun saveNextEpisodeThresholdPercent(percent: Float) = DesktopPrefs.putFloat("p_ne_p", percent)
    actual fun loadNextEpisodeThresholdMinutesBeforeEnd(): Float? = DesktopPrefs.getFloat("p_ne_min")
    actual fun saveNextEpisodeThresholdMinutesBeforeEnd(minutes: Float) = DesktopPrefs.putFloat("p_ne_min", minutes)
    actual fun loadPreloadNextEpisodeSources(): Boolean? = DesktopPrefs.getBoolean("p_ne_pre")
    actual fun savePreloadNextEpisodeSources(enabled: Boolean) = DesktopPrefs.putBoolean("p_ne_pre", enabled)
    actual fun loadUseLibass(): Boolean? = DesktopPrefs.getBoolean("p_libass")
    actual fun saveUseLibass(enabled: Boolean) = DesktopPrefs.putBoolean("p_libass", enabled)
    actual fun loadLibassRenderType(): String? = DesktopPrefs.getString("p_libass_rt")
    actual fun saveLibassRenderType(renderType: String) = DesktopPrefs.putString("p_libass_rt", renderType)
    actual fun loadLibassRenderScale(): String? = DesktopPrefs.getString("p_libass_rs")
    actual fun saveLibassRenderScale(scale: String) = DesktopPrefs.putString("p_libass_rs", scale)
    actual fun loadLibassRenderMode(): String? = DesktopPrefs.getString("p_libass_rm")
    actual fun saveLibassRenderMode(mode: String) = DesktopPrefs.putString("p_libass_rm", mode)
    actual fun loadIosVideoOutputPreset(): String? = null
    actual fun saveIosVideoOutputPreset(preset: String) {}
    actual fun loadIosToneMappingMode(): String? = null
    actual fun saveIosToneMappingMode(mode: String) {}
    actual fun loadIosTargetPrimaries(): String? = null
    actual fun saveIosTargetPrimaries(primaries: String) {}
    actual fun loadIosTargetTransfer(): String? = null
    actual fun saveIosTargetTransfer(transfer: String) {}
    actual fun loadIosHardwareDecoderMode(): String? = null
    actual fun saveIosHardwareDecoderMode(mode: String) {}
    actual fun loadIosAudioOutputMode(): String? = null
    actual fun saveIosAudioOutputMode(mode: String) {}
    actual fun loadIosExtendedDynamicRangeEnabled(): Boolean? = null
    actual fun saveIosExtendedDynamicRangeEnabled(enabled: Boolean) {}
    actual fun loadIosTargetColorspaceHintEnabled(): Boolean? = null
    actual fun saveIosTargetColorspaceHintEnabled(enabled: Boolean) {}
    actual fun loadIosHdrComputePeakEnabled(): Boolean? = null
    actual fun saveIosHdrComputePeakEnabled(enabled: Boolean) {}
    actual fun loadIosDebandEnabled(): Boolean? = null
    actual fun saveIosDebandEnabled(enabled: Boolean) {}
    actual fun loadIosInterpolationEnabled(): Boolean? = null
    actual fun saveIosInterpolationEnabled(enabled: Boolean) {}
    actual fun loadIosBrightness(): Int? = null
    actual fun saveIosBrightness(value: Int) {}
    actual fun loadIosContrast(): Int? = null
    actual fun saveIosContrast(value: Int) {}
    actual fun loadIosSaturation(): Int? = null
    actual fun saveIosSaturation(value: Int) {}
    actual fun loadIosGamma(): Int? = null
    actual fun saveIosGamma(value: Int) {}
    actual fun exportToSyncPayload(): JsonObject = buildJsonObject {}
    actual fun replaceFromSyncPayload(payload: JsonObject) {}
}



