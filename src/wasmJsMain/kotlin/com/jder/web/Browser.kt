@file:OptIn(ExperimentalWasmJsInterop::class)

package com.jder.web

import kotlin.io.encoding.Base64
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.Promise
import kotlinx.coroutines.await

internal fun isMacPlatform(): Boolean = js("navigator.platform.startsWith('Mac')")

internal fun browserLanguage(): String = js("navigator.language")

internal fun consoleError(message: String): Unit = js("console.error(message)")

internal fun writeClipboard(text: String): Unit =
    js("navigator.clipboard.writeText(text).catch((error) => console.error(error))")

@JsFun("(key) => localStorage.getItem(key)")
internal external fun localStorageItem(key: String): String?

internal fun setLocalStorageItem(key: String, value: String): Unit =
    js("localStorage.setItem(key, value)")

internal fun removeElement(id: String): Unit = js("document.getElementById(id)?.remove()")

internal fun warnBeforeUnload(hasUnsavedChanges: () -> Boolean): Unit =
    js(
        """window.addEventListener('beforeunload', (event) => {
            if (hasUnsavedChanges()) {
                event.preventDefault();
                event.returnValue = '';
            }
        })""",
    )

internal fun isDocumentFullscreen(): Boolean = js("document.fullscreenElement != null")

internal fun toggleDocumentFullscreen(): Unit =
    js(
        """(document.fullscreenElement
            ? document.exitFullscreen()
            : document.documentElement.requestFullscreen()
        ).catch((error) => console.error(error))""",
    )

internal fun onFullscreenChange(callback: () -> Unit): Unit =
    js("document.addEventListener('fullscreenchange', () => callback())")

internal fun pickTextFile(
    accept: String,
    onPicked: (String, String) -> Unit,
    onFailed: (String) -> Unit,
): Unit =
    js(
        """{
            const input = document.createElement('input');
            input.type = 'file';
            input.accept = accept;
            input.addEventListener('change', () => {
                const file = input.files[0];
                if (file) {
                    file.text().then(
                        (text) => onPicked(file.name, text),
                        (error) => onFailed(String(error)),
                    );
                }
            });
            input.click();
        }""",
    )

internal fun downloadFile(name: String, type: String, bytes: ByteArray) =
    downloadBase64(name, type, Base64.encode(bytes))

internal suspend fun fetchBytes(path: String): ByteArray =
    Base64.decode(fetchBase64(path).await<JsString>().toString())

private fun downloadBase64(name: String, type: String, base64: String): Unit =
    js(
        """{
            const binary = atob(base64);
            const bytes = new Uint8Array(binary.length);
            for (let index = 0; index < binary.length; index++) {
                bytes[index] = binary.charCodeAt(index);
            }
            const link = document.createElement('a');
            link.href = URL.createObjectURL(new Blob([bytes], { type }));
            link.download = name;
            link.click();
            setTimeout(() => URL.revokeObjectURL(link.href), 60000);
        }""",
    )

private fun fetchBase64(path: String): Promise<JsString> =
    js(
        """fetch(path)
            .then((response) => {
                if (!response.ok) throw new Error(response.status + ' ' + path);
                return response.arrayBuffer();
            })
            .then((buffer) => {
                const bytes = new Uint8Array(buffer);
                let binary = '';
                for (let index = 0; index < bytes.length; index += 32768) {
                    binary += String.fromCharCode(...bytes.subarray(index, index + 32768));
                }
                return btoa(binary);
            })""",
    )
