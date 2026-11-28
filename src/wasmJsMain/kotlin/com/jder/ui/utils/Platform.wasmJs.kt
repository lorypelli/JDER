package com.jder.ui.utils

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import com.jder.strings.CommonStrings
import com.jder.ui.components.Toasts
import com.jder.web.browserLanguage
import com.jder.web.fetchBytes
import com.jder.web.isMacPlatform
import com.jder.web.sharedValues
import com.jder.web.writeClipboard
import kotlin.js.toList

actual val isMacOS: Boolean = isMacPlatform()

actual val usesSystemMenuBar: Boolean = false

actual val showsToolbarMenus: Boolean = true

actual val systemLanguage: String
    get() = browserLanguage().substringBefore('-')

actual fun copyToClipboard(text: String) =
    writeClipboard(text) { Toasts.error(CommonStrings.COPY_FAILED) }

actual suspend fun readResource(path: String): ByteArray = fetchBytes("/$path")

actual fun fontFromBytes(name: String, bytes: ByteArray, weight: FontWeight): Font =
    Font(name, bytes, weight)

actual fun sharedList(name: String): List<String> =
    sharedValues(name).toList().map { it.toString() }
