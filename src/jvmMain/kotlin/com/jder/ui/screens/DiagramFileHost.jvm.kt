package com.jder.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.ImageBitmap
import com.jder.data.DiagramFileRepository
import com.jder.data.exportPng
import com.jder.data.loadDiagram
import com.jder.data.saveDiagram
import com.jder.domain.model.DiagramEditorState
import com.jder.strings.FileStrings
import com.jder.strings.Localization
import com.jder.ui.components.DiagramPalette
import com.jder.ui.components.FileManagerDialog
import com.jder.ui.components.FileManagerMode
import com.jder.ui.components.Toasts
import com.jder.ui.theme.ThemeState
import java.io.File
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

private val autoSaveDelay = 500.milliseconds
private val imageExportLock = Any()

@Composable
actual fun <T> DiagramFileHost(
    state: DiagramEditorState<T>,
    repository: DiagramFileRepository<T>,
    themeState: ThemeState,
    renderImage: (DiagramPalette) -> ImageBitmap,
    content: @Composable (DiagramFileActions) -> Unit,
) {
    var showOpenDialog by remember { mutableStateOf(false) }
    var showSaveAsDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    val currentRenderImage by rememberUpdatedState(renderImage)
    fun currentImageKey(): Any =
        Triple(state.imageKey, themeState.selectedPalette, Localization.language)
    fun exportImage() = renderExportImage(themeState, currentRenderImage)
    fun writeAutoExport(file: File, key: Any, image: () -> ImageBitmap): Result<Unit> =
        if (key == state.exportedImageKey) Result.success(Unit)
        else exportPng(image(), file).onSuccess { state.exportedImageKey = key }
    fun writeDiagram(file: File): Result<Unit> {
        val name = file.nameWithoutExtension
        return repository.saveDiagram(state.diagramNamed(name), file).onSuccess {
            state.updateDiagramName(name)
            state.markAsSaved(file.absolutePath)
        }
    }
    fun autoSaveFile(): File? =
        state.currentFile?.takeIf { state.isAutoSaveEnabled && state.isModified }?.let(::File)
    fun autoExportFile(key: Any): File? =
        state.imageFile
            ?.takeIf { state.isAutoExportEnabled && key != state.exportedImageKey }
            ?.let(::File)
    fun autoSave(file: File): Boolean =
        writeDiagram(file).onFailure { Toasts.error(FileStrings.AUTO_SAVE_FAILED, it) }.isSuccess
    fun reportAutoExport(result: Result<Unit>): Boolean =
        result.onFailure { Toasts.error(FileStrings.AUTO_EXPORT_FAILED, it) }.isSuccess
    fun flushAutoSave(): Boolean {
        val isSaved = autoSaveFile()?.let(::autoSave) ?: true
        val key = currentImageKey()
        val isExported =
            autoExportFile(key)?.let { file ->
                val result =
                    try {
                        synchronized(imageExportLock) {
                            writeAutoExport(file, key, ::exportImage)
                        }
                    } catch (e: Exception) {
                        Result.failure(e)
                    }
                reportAutoExport(result)
            } ?: true
        return isSaved && isExported
    }
    fun saveTo(file: File) {
        writeDiagram(file)
            .fold(
                onSuccess = { Toasts.success(FileStrings.diagramSaved(file.name)) },
                onFailure = { Toasts.error(FileStrings.SAVE_FAILED, it) },
            )
    }
    fun saveDiagram() {
        state.currentFile?.let { saveTo(File(it)) } ?: run { showSaveAsDialog = true }
    }
    fun exportTo(file: File) {
        val key = currentImageKey()
        val result =
            try {
                val image = exportImage()
                synchronized(imageExportLock) {
                    exportPng(image, file).onSuccess {
                        state.markAsExported(file.absolutePath, key)
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        result.fold(
            onSuccess = { Toasts.success(FileStrings.imageSaved(file.name)) },
            onFailure = { Toasts.error(FileStrings.EXPORT_FAILED, it) },
        )
    }
    fun toggleAutoSave() {
        val file = File(state.currentFile ?: return)
        state.isAutoSaveEnabled = !state.isAutoSaveEnabled
        Toasts.success(
            if (state.isAutoSaveEnabled) FileStrings.autoSaveEnabled(file.name)
            else FileStrings.AUTO_SAVE_DISABLED,
        )
    }
    fun toggleAutoExport() {
        val file = File(state.imageFile ?: return)
        state.isAutoExportEnabled = !state.isAutoExportEnabled
        Toasts.success(
            if (state.isAutoExportEnabled) FileStrings.autoExportEnabled(file.name)
            else FileStrings.AUTO_EXPORT_DISABLED,
        )
    }
    LaunchedEffect(state) {
        snapshotFlow {
            listOf(state.isAutoSaveEnabled, state.isModified, state.currentFile, state.diagram)
        }
            .collectLatest {
                delay(autoSaveDelay)
                autoSaveFile()?.let(::autoSave)
            }
    }
    LaunchedEffect(state) {
        snapshotFlow { Triple(state.isAutoExportEnabled, state.imageFile, currentImageKey()) }
            .collectLatest {
                delay(autoSaveDelay)
                val key = currentImageKey()
                val file = autoExportFile(key) ?: return@collectLatest
                val image =
                    try {
                        exportImage()
                    } catch (e: Exception) {
                        reportAutoExport(Result.failure(e))
                        return@collectLatest
                    }
                state.exportedImageKey = null
                val result =
                    withContext(Dispatchers.IO) {
                        synchronized(imageExportLock) {
                            ensureActive()
                            writeAutoExport(file, key) { image }
                        }
                    }
                reportAutoExport(result)
            }
    }
    DisposableEffect(state) { onDispose { flushAutoSave() } }
    content(
        DiagramFileActions(
            open = { showOpenDialog = true },
            save = ::saveDiagram,
            saveAs = { showSaveAsDialog = true },
            export = { showExportDialog = true },
            toggleAutoSave = ::toggleAutoSave,
            toggleAutoExport = ::toggleAutoExport,
            flushAutoSave = ::flushAutoSave,
        ),
    )
    if (showOpenDialog) {
        FileManagerDialog(
            mode = FileManagerMode.OPEN,
            fileExtension = ".json",
            title = FileStrings.OPEN_DIAGRAM,
            onDismiss = { showOpenDialog = false },
            onFileSelected = { file ->
                showOpenDialog = false
                repository
                    .loadDiagram(file)
                    .fold(
                        onSuccess = {
                            state.loadDiagram(it, file.absolutePath)
                            Toasts.success(FileStrings.diagramLoaded(file.name))
                        },
                        onFailure = { Toasts.error(FileStrings.LOAD_FAILED, it) },
                    )
            },
        )
    }
    if (showSaveAsDialog) {
        FileManagerDialog(
            mode = FileManagerMode.SAVE,
            fileExtension = ".json",
            title = FileStrings.SAVE_DIAGRAM,
            defaultFileName = state.diagramName,
            onDismiss = { showSaveAsDialog = false },
            onFileSelected = {
                showSaveAsDialog = false
                saveTo(it)
            },
        )
    }
    if (showExportDialog) {
        FileManagerDialog(
            mode = FileManagerMode.SAVE,
            fileExtension = ".png",
            title = FileStrings.EXPORT_PNG,
            defaultFileName = state.diagramName,
            onDismiss = { showExportDialog = false },
            onFileSelected = {
                showExportDialog = false
                exportTo(it)
            },
        )
    }
}
