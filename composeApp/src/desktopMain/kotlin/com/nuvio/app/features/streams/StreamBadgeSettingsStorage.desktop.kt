package com.nuvio.app.features.streams

import com.nuvio.app.DesktopPrefs
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

internal actual object StreamBadgeSettingsStorage {
    actual fun loadStreamBadgeRules(): String? = DesktopPrefs.getString("sb_rules")
    actual fun saveStreamBadgeRules(rules: String) = DesktopPrefs.putString("sb_rules", rules)
    actual fun loadShowFileSizeBadges(): Boolean? = DesktopPrefs.getBoolean("sb_size")
    actual fun saveShowFileSizeBadges(enabled: Boolean) = DesktopPrefs.putBoolean("sb_size", enabled)
    actual fun loadShowAddonLogo(): Boolean? = DesktopPrefs.getBoolean("sb_logo")
    actual fun saveShowAddonLogo(enabled: Boolean) = DesktopPrefs.putBoolean("sb_logo", enabled)
    actual fun loadShowStreamSearch(): Boolean? = DesktopPrefs.getBoolean("sb_search")
    actual fun saveShowStreamSearch(enabled: Boolean) = DesktopPrefs.putBoolean("sb_search", enabled)
    actual fun loadShowInPlayer(): Boolean? = DesktopPrefs.getBoolean("sb_inplayer")
    actual fun saveShowInPlayer(enabled: Boolean) = DesktopPrefs.putBoolean("sb_inplayer", enabled)
    actual fun loadPlayerBadgeSource(): String? = DesktopPrefs.getString("sb_src")
    actual fun savePlayerBadgeSource(source: String) = DesktopPrefs.putString("sb_src", source)
    actual fun loadStreamBackgroundMode(): String? = DesktopPrefs.getString("sb_bg")
    actual fun saveStreamBackgroundMode(mode: String) = DesktopPrefs.putString("sb_bg", mode)
    actual fun loadStreamBadgePlacement(): String? = DesktopPrefs.getString("sb_place")
    actual fun saveStreamBadgePlacement(placement: String) = DesktopPrefs.putString("sb_place", placement)
    actual fun loadLegacyDebridStreamBadgeRules(): String? = DesktopPrefs.getString("sb_leg")
    actual fun clearLegacyDebridStreamBadgeRules() = DesktopPrefs.putString("sb_leg", null)
    actual fun exportToSyncPayload(): JsonObject = buildJsonObject {}
    actual fun replaceFromSyncPayload(payload: JsonObject) {}
}
