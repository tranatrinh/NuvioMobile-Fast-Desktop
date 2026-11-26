package com.nuvio.app

import java.util.prefs.Preferences

internal object DesktopPrefs {
    private val prefs: Preferences = Preferences.userRoot().node("com.nuvio.app.fast")
    fun getString(key: String): String? = prefs.get(key, null)
    fun putString(key: String, value: String?) {
        if (value == null) prefs.remove(key) else prefs.put(key, value)
    }
    fun getBoolean(key: String): Boolean? = if (prefs.get(key, null) != null) prefs.getBoolean(key, false) else null
    fun putBoolean(key: String, value: Boolean) = prefs.putBoolean(key, value)
    fun getInt(key: String): Int? = if (prefs.get(key, null) != null) prefs.getInt(key, 0) else null
    fun putInt(key: String, value: Int) = prefs.putInt(key, value)
    fun getFloat(key: String): Float? = if (prefs.get(key, null) != null) prefs.getFloat(key, 0f) else null
    fun putFloat(key: String, value: Float) = prefs.putFloat(key, value)
    fun getStringSet(key: String): Set<String>? = prefs.get(key, null)?.split("\n")?.filter { it.isNotEmpty() }?.toSet()
    fun putStringSet(key: String, value: Set<String>) = prefs.put(key, value.joinToString("\n"))
}
