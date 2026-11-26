package com.nuvio.app.features.livetv

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nuvio.app.DesktopPrefs
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

internal actual val liveTvBackToTopBottomPadding: Dp = 16.dp

@Composable
internal actual fun rememberLiveTvPlaylistFilePicker(
    onPlaylistLoaded: (fileName: String?, content: String) -> Unit,
    onError: (String) -> Unit,
): LiveTvPlaylistFilePicker = LiveTvPlaylistFilePicker(canPickFiles = true) {
    val fd = FileDialog(null as Frame?, "Chon file M3U", FileDialog.LOAD)
    fd.isVisible = true
    val file = fd.file ?: return@LiveTvPlaylistFilePicker
    val dir = fd.directory ?: ""
    runCatching { File(dir, file).readText() }
        .onSuccess { onPlaylistLoaded(file, it) }
        .onFailure { onError(it.message ?: "Loi doc file") }
}

internal actual object LiveTvStorage {
    actual fun loadPlaylistUrl(): String? = DesktopPrefs.getString("ltv_url")
    actual fun savePlaylistUrl(url: String) = DesktopPrefs.putString("ltv_url", url)
    actual fun loadPlaylistsBlob(): String? = DesktopPrefs.getString("ltv_playlists")
    actual fun savePlaylistsBlob(blob: String) = DesktopPrefs.putString("ltv_playlists", blob)
    actual fun loadFavoriteChannelIdsBlob(): String? = DesktopPrefs.getString("ltv_favs")
    actual fun saveFavoriteChannelIdsBlob(blob: String) = DesktopPrefs.putString("ltv_favs", blob)
    actual fun loadRecentChannelIdsBlob(): String? = DesktopPrefs.getString("ltv_recent")
    actual fun saveRecentChannelIdsBlob(blob: String) = DesktopPrefs.putString("ltv_recent", blob)
    actual fun loadLastWatchedChannelId(): String? = DesktopPrefs.getString("ltv_last_ch")
    actual fun saveLastWatchedChannelId(channelId: String) = DesktopPrefs.putString("ltv_last_ch", channelId)
    actual fun loadLastSelectedPlaylistId(): String? = DesktopPrefs.getString("ltv_last_pl")
    actual fun saveLastSelectedPlaylistId(playlistId: String?) = DesktopPrefs.putString("ltv_last_pl", playlistId)
    actual fun loadNavigationEnabled(): Boolean? = DesktopPrefs.getBoolean("ltv_nav")
    actual fun saveNavigationEnabled(enabled: Boolean) = DesktopPrefs.putBoolean("ltv_nav", enabled)
    actual fun loadStalkerSettings(): LiveTvStalkerSettings = LiveTvStalkerSettings()
    actual fun saveStalkerSettings(settings: LiveTvStalkerSettings) {}
    actual fun loadXtreamSettings(): LiveTvXtreamSettings = LiveTvXtreamSettings()
    actual fun saveXtreamSettings(settings: LiveTvXtreamSettings) {}
    actual fun publishNavigationVisibility(visible: Boolean) {}
}
