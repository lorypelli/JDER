package com.jder.data

import com.jder.web.localStorageItem
import com.jder.web.setLocalStorageItem
import com.jder.web.storageKey

actual fun readPreference(key: String): String? = localStorageItem(storageKey(key))

actual fun savePreference(key: String, value: String, onFailure: (Throwable) -> Unit) {
    try {
        setLocalStorageItem(storageKey(key), value)
    } catch (e: Throwable) {
        onFailure(e)
    }
}
