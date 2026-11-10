package com.jder.data

import java.util.prefs.Preferences

object AppPreferences {
    val node: Preferences by lazy { Preferences.userRoot().node("com/jder") }

    fun saveSafely(onFailure: (Exception) -> Unit, write: Preferences.() -> Unit) {
        try {
            node.write()
            node.flush()
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}
