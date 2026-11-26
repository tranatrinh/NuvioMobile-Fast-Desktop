package com.nuvio.app.features.settings

import androidx.compose.foundation.lazy.LazyListScope
import com.nuvio.app.DesktopPrefs
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

internal actual object AppIconPlatform {
    actual val requiresCloseConfirmation: Boolean = false
    actual fun loadSelectedIconKey(): String? = DesktopPrefs.getString("app_icon")
    actual fun saveSelectedIconKey(key: String) = DesktopPrefs.putString("app_icon", key)
    actual fun currentIconName(): String? = null
    actual suspend fun activateIcon(name: String?): Boolean = false
}

internal actual fun LazyListScope.pluginsSettingsContent() {}

internal actual object SentrySettingsPlatform {
    actual val crashReportsSupported: Boolean = false
}

internal actual object SentrySettingsStorage {
    actual fun loadEnabled(): Boolean? = false
    actual fun saveEnabled(enabled: Boolean) {}
}

internal actual object ThemeSettingsStorage {
    actual fun loadSelectedTheme(): String? = DesktopPrefs.getString("th_sel")
    actual fun saveSelectedTheme(themeName: String) = DesktopPrefs.putString("th_sel", themeName)
    actual fun loadCustomThemeColors(): String? = DesktopPrefs.getString("th_cust")
    actual fun saveCustomThemeColors(colors: String) = DesktopPrefs.putString("th_cust", colors)
    actual fun loadLowEndModeEnabled(): Boolean? = DesktopPrefs.getBoolean("th_low")
    actual fun saveLowEndModeEnabled(enabled: Boolean) = DesktopPrefs.putBoolean("th_low", enabled)
    actual fun loadAmoledEnabled(): Boolean? = DesktopPrefs.getBoolean("th_amoled")
    actual fun saveAmoledEnabled(enabled: Boolean) = DesktopPrefs.putBoolean("th_amoled", enabled)
    actual fun loadLiquidGlassNativeTabBarEnabled(): Boolean? = false
    actual fun saveLiquidGlassNativeTabBarEnabled(enabled: Boolean) {}
    actual fun loadSelectedAppLanguage(): String? = DesktopPrefs.getString("th_lang")
    actual fun saveSelectedAppLanguage(languageCode: String) = DesktopPrefs.putString("th_lang", languageCode)
    actual fun applySelectedAppLanguage(languageCode: String) {}
    actual fun loadNavBarStyle(): String? = DesktopPrefs.getString("th_nav_st")
    actual fun saveNavBarStyle(styleKey: String) = DesktopPrefs.putString("th_nav_st", styleKey)
    actual fun loadNavBarGlowEnabled(): Boolean? = DesktopPrefs.getBoolean("th_nav_gl")
    actual fun saveNavBarGlowEnabled(enabled: Boolean) = DesktopPrefs.putBoolean("th_nav_gl", enabled)
    actual fun exportToSyncPayload(): JsonObject = buildJsonObject {}
    actual fun replaceFromSyncPayload(payload: JsonObject) {}
}
