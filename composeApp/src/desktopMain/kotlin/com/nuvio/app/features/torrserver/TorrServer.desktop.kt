package com.nuvio.app.features.torrserver

import com.nuvio.app.DesktopPrefs

internal actual object TorrServerSettingsStorage {
    actual fun isInitialized(): Boolean = true
    actual fun loadEnabled(): Boolean? = DesktopPrefs.getBoolean("ts_en")
    actual fun saveEnabled(enabled: Boolean) = DesktopPrefs.putBoolean("ts_en", enabled)
    actual fun loadServerUrl(): String? = DesktopPrefs.getString("ts_url")
    actual fun saveServerUrl(url: String) = DesktopPrefs.putString("ts_url", url)
    actual fun loadAuthUsername(): String? = DesktopPrefs.getString("ts_user")
    actual fun saveAuthUsername(username: String) = DesktopPrefs.putString("ts_user", username)
    actual fun loadAuthPassword(): String? = DesktopPrefs.getString("ts_pass")
    actual fun saveAuthPassword(password: String) = DesktopPrefs.putString("ts_pass", password)
    actual fun loadPreload(): Boolean? = DesktopPrefs.getBoolean("ts_pre")
    actual fun savePreload(preload: Boolean) = DesktopPrefs.putBoolean("ts_pre", preload)
    actual fun loadSaveToDb(): Boolean? = DesktopPrefs.getBoolean("ts_db")
    actual fun saveSaveToDb(saveToDb: Boolean) = DesktopPrefs.putBoolean("ts_db", saveToDb)
    actual fun loadGst(): Boolean? = DesktopPrefs.getBoolean("ts_gst")
    actual fun saveGst(gst: Boolean) = DesktopPrefs.putBoolean("ts_gst", gst)
}

internal actual suspend fun runTorrServerPreloadStream(url: String, headers: Map<String, String>) {}
