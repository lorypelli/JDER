package com.jder.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import com.jder.data.DiagramFileRepository
import com.jder.data.encodePng
import com.jder.domain.model.DiagramEditorState
import com.jder.strings.FileStrings
import com.jder.ui.components.DiagramPalette
import com.jder.ui.components.Toasts
import com.jder.ui.dialogs.FileNameDialog
import com.jder.ui.theme.ThemeState
import com.jder.web.downloadFile
import com.jder.web.pickTextFile

private enum class SaveRequest(val extension: String) {
    DIAGRAM(".json"),
    IMAGE(".png"),
}

@Composable
actual fun <T> DiagramFileHost(
    state: DiagramEditorState<T>,
    repository: DiagramFileRepository<T>,
    themeState: ThemeState,
    renderImage: (DiagramPalette) -> ImageBitmap,
    content: @Composable (DiagramFileActions) -> Unit,
) {
    var saveRequest by remember { mutableStateOf<SaveRequest?>(null) }
    val currentRenderImage by rememberUpdatedState(renderImage)
    fun saveDiagram(fileName: String) {
        val name = fileName.substringBeforeLast('.')
        try {
            val json = repository.encodeDiagram(state.diagramNamed(name))
            downloadFile(fileName, "application/json", json.encodeToByteArray())
            state.updateDiagramName(name)
            state.markAsSaved(fileName)
            Toasts.success(FileStrings.diagramSaved(fileName))
        } catch (e: Exception) {
            Toasts.error(FileStrings.SAVE_FAILED, e)
        }
    }
    fun exportImage(fileName: String) {
        try {
            val image = renderExportImage(themeState, currentRenderImage)
            downloadFile(fileName, "image/png", encodePng(image))
            Toasts.success(FileStrings.imageSaved(fileName))
        } catch (e: Exception) {
            Toasts.error(FileStrings.EXPORT_FAILED, e)
        }
    }
    fun openDiagram() {
        pickTextFile(
            accept = ".json,application/json",
            onPicked = { fileName, text ->
                try {
                    state.loadDiagram(repository.decodeDiagram(text), fileName)
                    Toasts.success(FileStrings.diagramLoaded(fileName))
                } catch (e: Exception) {
                    Toasts.error(FileStrings.LOAD_FAILED, e)
                }
            },
            onFailed = { Toasts.error(FileStrings.LOAD_FAILED, Exception(it)) },
        )
    }
    content(
        DiagramFileActions(
            open = ::openDiagram,
            save = {
                state.currentFile?.let(::saveDiagram) ?: run { saveRequest = SaveRequest.DIAGRAM }
            },
            saveAs = { saveRequest = SaveRequest.DIAGRAM },
            export = { saveRequest = SaveRequest.IMAGE },
            toggleAutoSave = null,
            toggleAutoExport = null,
            flushAutoSave = { true },
        ),
    )
    saveRequest?.let { request ->
        FileNameDialog(
            title =
                when (request) {
                    SaveRequest.DIAGRAM -> FileStrings.SAVE_DIAGRAM
                    SaveRequest.IMAGE -> FileStrings.EXPORT_PNG
                },
            initialName = state.diagramName,
            extension = request.extension,
            onDismiss = { saveRequest = null },
            onConfirm = {
                saveRequest = null
                when (request) {
                    SaveRequest.DIAGRAM -> saveDiagram(it)
                    SaveRequest.IMAGE -> exportImage(it)
                }
            },
        )
    }
}
