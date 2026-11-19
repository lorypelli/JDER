package com.jder.data

import com.jder.web.localStorageItem
import com.jder.web.setLocalStorageItem

private const val KEY_PREFIX = "jder-"
private val uppercase = Regex("[A-Z]")

private fun storageKey(key: String) =
    KEY_PREFIX + key.replace(uppercase) { "-${it.value.lowercase()}" }

actual fun readPreference(key: String): String? = localStorageItem(storageKey(key))

actual fun savePreference(key: String, value: String, onFailure: (Throwable) -> Unit) {
    try {
        setLocalStorageItem(storageKey(key), value)
    } catch (e: Throwable) {
        onFailure(e)
    }
}
