package com.jder.data

import com.jder.web.localStorageItem
import com.jder.web.setLocalStorageItem

private const val KEY_PREFIX = "jder-app-"

actual fun readPreference(key: String): String? = localStorageItem(KEY_PREFIX + key)

actual fun savePreference(key: String, value: String, onFailure: (Throwable) -> Unit) {
    try {
        setLocalStorageItem(KEY_PREFIX + key, value)
    } catch (e: Throwable) {
        onFailure(e)
    }
}
