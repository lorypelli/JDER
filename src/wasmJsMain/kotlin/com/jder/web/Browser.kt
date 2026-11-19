package com.jder.web

import com.jder.strings.CommonStrings
import com.jder.ui.components.Toasts
import kotlin.js.JsAny
import kotlin.js.JsString
import kotlin.js.toJsArray
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.toByteArray
import org.khronos.webgl.toInt8Array
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.url.URL
import org.w3c.fetch.Response
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.FileReader

private const val OBJECT_URL_LIFETIME_MS = 60_000

internal fun isMacPlatform(): Boolean = window.navigator.platform.startsWith("Mac")

internal fun browserLanguage(): String = window.navigator.language

internal external fun syncTheme()

internal fun writeClipboard(text: String) {
    window.navigator.clipboard.writeText(text).catch {
        Toasts.error(CommonStrings.COPY_FAILED)
        null
    }
}

internal fun localStorageItem(key: String): String? = localStorage.getItem(key)

internal fun setLocalStorageItem(key: String, value: String) = localStorage.setItem(key, value)

internal fun warnBeforeUnload(hasUnsavedChanges: () -> Boolean) {
    window.addEventListener("beforeunload") { event ->
        if (hasUnsavedChanges()) {
            event.preventDefault()
        }
    }
}

internal fun isDocumentFullscreen(): Boolean = document.fullscreenElement != null

internal fun toggleDocumentFullscreen() {
    val request =
        if (document.fullscreenElement != null) document.exitFullscreen()
        else document.documentElement?.requestFullscreen()
    request?.catch { null }
}

internal fun onFullscreenChange(callback: () -> Unit) {
    document.addEventListener("fullscreenchange") { callback() }
}

internal fun pickTextFile(
    accept: String,
    onPicked: (String, String) -> Unit,
    onFailed: (String) -> Unit,
) {
    val input = document.createElement("input") as HTMLInputElement
    input.type = "file"
    input.accept = accept
    input.addEventListener("change") {
        val file = input.files?.item(0) ?: return@addEventListener
        val reader = FileReader()
        reader.onload = { onPicked(file.name, (reader.result as JsString).toString()) }
        reader.onerror = { onFailed(reader.error.toString()) }
        reader.readAsText(file)
    }
    input.click()
}

internal fun downloadFile(name: String, type: String, bytes: ByteArray) {
    val blob = Blob(listOf<JsAny?>(bytes.toInt8Array()).toJsArray(), BlobPropertyBag(type))
    val url = URL.createObjectURL(blob)
    val link = document.createElement("a") as HTMLAnchorElement
    link.href = url
    link.download = name
    link.click()
    window.setTimeout(
        {
            URL.revokeObjectURL(url)
            null
        },
        OBJECT_URL_LIFETIME_MS,
    )
}

internal suspend fun fetchBytes(path: String): ByteArray {
    val response = window.fetch(path).await<Response>()
    if (!response.ok) error("${response.status} $path")
    return Int8Array(response.arrayBuffer().await<ArrayBuffer>()).toByteArray()
}
