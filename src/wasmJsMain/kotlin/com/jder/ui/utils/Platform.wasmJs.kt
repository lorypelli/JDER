package com.jder.ui.utils

import com.jder.web.browserLanguage
import com.jder.web.consoleError
import com.jder.web.fetchBytes
import com.jder.web.isMacPlatform
import com.jder.web.writeClipboard

actual val isMacOS: Boolean = isMacPlatform()

actual val usesSystemMenuBar: Boolean = false

actual val showsToolbarMenus: Boolean = true

actual val systemLanguage: String
    get() = browserLanguage().substringBefore('-')

actual fun copyToClipboard(text: String) = writeClipboard(text)

actual fun printError(message: String) = consoleError(message)

actual suspend fun readResource(path: String): ByteArray = fetchBytes("/$path")
