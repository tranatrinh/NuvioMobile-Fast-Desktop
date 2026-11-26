package com.nuvio.app.features.home

import com.nuvio.app.DesktopPrefs

internal actual object HomeHeroStorage {
    actual fun loadPayload(): String? = DesktopPrefs.getString("home_hero")
    actual fun savePayload(payload: String) = DesktopPrefs.putString("home_hero", payload)
}
