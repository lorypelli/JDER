package com.jder.data

import java.util.prefs.Preferences

private val preferences: Preferences by lazy { Preferences.userRoot().node("com/jder") }

actual fun readPreference(key: String): String? = preferences.get(key, null)

actual fun savePreference(key: String, value: String, onFailure: (Throwable) -> Unit) {
    try {
        preferences.put(key, value)
        preferences.flush()
    } catch (e: Exception) {
        onFailure(e)
    }
}
