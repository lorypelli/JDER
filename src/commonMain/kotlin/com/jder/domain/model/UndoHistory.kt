package com.jder.domain.model

import androidx.compose.runtime.mutableStateListOf

class UndoHistory<T>(private val maxSize: Int = 50) {
    private val undoStack = mutableStateListOf<T>()
    private val redoStack = mutableStateListOf<T>()

    val canUndo: Boolean
        get() = undoStack.isNotEmpty()

    val canRedo: Boolean
        get() = redoStack.isNotEmpty()

    fun record(snapshot: T) {
        undoStack.add(snapshot)
        if (undoStack.size > maxSize) undoStack.removeAt(0)
        redoStack.clear()
    }

    fun undo(current: T): T? {
        val previous = undoStack.removeLastOrNull() ?: return null
        redoStack.add(current)
        return previous
    }

    fun redo(current: T): T? {
        val next = redoStack.removeLastOrNull() ?: return null
        undoStack.add(current)
        return next
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}
