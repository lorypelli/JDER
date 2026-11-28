package com.jder.web

import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.toByteArray
import org.khronos.webgl.toInt8Array
import org.w3c.fetch.Response

internal external fun isMacPlatform(): Boolean

internal external fun browserLanguage(): String

internal external fun applyTheme(isDarkTheme: Boolean)

internal external fun focusApp()

internal external fun writeClipboard(text: String, onFailed: () -> Unit)

internal external fun localStorageItem(key: String): String?

internal external fun setLocalStorageItem(key: String, value: String)

internal external fun warnBeforeUnload(hasUnsavedChanges: () -> Boolean)

internal external fun isDocumentFullscreen(): Boolean

internal external fun toggleDocumentFullscreen()

internal external fun onFullscreenChange(callback: () -> Unit)

internal external fun pickTextFile(
    accept: String,
    onPicked: (String, String) -> Unit,
    onFailed: (String) -> Unit,
)

private external fun downloadBlob(name: String, type: String, bytes: Int8Array)

internal fun downloadFile(name: String, type: String, bytes: ByteArray) =
    downloadBlob(name, type, bytes.toInt8Array())

internal suspend fun fetchBytes(path: String): ByteArray {
    val response = window.fetch(path).await<Response>()
    if (!response.ok) error("${response.status} $path")
    return Int8Array(response.arrayBuffer().await<ArrayBuffer>()).toByteArray()
}
