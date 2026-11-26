package com.nuvio.app.features.p2p

import com.nuvio.app.DesktopPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal actual object P2pSettingsStorage {
    actual fun loadP2pEnabled(): Boolean? = DesktopPrefs.getBoolean("p2p_en")
    actual fun saveP2pEnabled(enabled: Boolean) = DesktopPrefs.putBoolean("p2p_en", enabled)
    actual fun loadEnableUpload(): Boolean? = DesktopPrefs.getBoolean("p2p_up")
    actual fun saveEnableUpload(enabled: Boolean) = DesktopPrefs.putBoolean("p2p_up", enabled)
    actual fun loadHideTorrentStats(): Boolean? = DesktopPrefs.getBoolean("p2p_hide")
    actual fun saveHideTorrentStats(enabled: Boolean) = DesktopPrefs.putBoolean("p2p_hide", enabled)
    actual fun loadTorrentProfile(): String? = DesktopPrefs.getString("p2p_prof")
    actual fun saveTorrentProfile(profile: String) = DesktopPrefs.putString("p2p_prof", profile)
    actual fun loadCacheSize(): String? = DesktopPrefs.getString("p2p_cache")
    actual fun saveCacheSize(size: String) = DesktopPrefs.putString("p2p_cache", size)
}

actual object P2pStreamingEngine {
    actual val state: StateFlow<P2pStreamingState> = MutableStateFlow(P2pStreamingState.Idle)
    actual val cacheState: StateFlow<P2pCacheUiState> = MutableStateFlow(P2pCacheUiState())
    actual suspend fun startStream(request: P2pStreamRequest): String = ""
    actual suspend fun clearCache(): P2pCacheClearResult = P2pCacheClearResult(reclaimedBytes = 0L, remainingBytes = 0L, protectedBytes = 0L)
    actual fun stopStream() {}
    actual fun shutdown() {}
}
