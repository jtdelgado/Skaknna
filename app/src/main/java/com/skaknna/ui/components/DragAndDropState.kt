package com.skaknna.ui.components

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// ─── Drag source types ─────────────────────────────────────────────────────────

sealed class DragSource {
    /** Piece being dragged from within the board */
    data class FromBoard(val row: Int, val col: Int) : DragSource()
    /** Piece being dragged from a palette (not yet on the board) */
    data class FromPalette(val isBlack: Boolean) : DragSource()
}

// ─── Active drag state ─────────────────────────────────────────────────────────

/**
 * Holds all information about the piece currently being dragged.
 *
 * @param piece     FEN character of the piece (e.g. 'K', 'q', 'P')
 * @param source    Where the drag originated
 * @param screenX   Absolute X on screen (px) — follows the finger
 * @param screenY   Absolute Y on screen (px) — follows the finger
 */
data class ActiveDrag(
    val piece: Char,
    val source: DragSource,
    val screenX: Float,
    val screenY: Float
)

// ─── DragAndDropState ──────────────────────────────────────────────────────────

class DragAndDropState {
    /** Non-null while a drag gesture is in progress */
    var activeDrag: ActiveDrag? by mutableStateOf(null)
        private set

    fun startDrag(piece: Char, source: DragSource, screenX: Float, screenY: Float) {
        activeDrag = ActiveDrag(piece, source, screenX, screenY)
    }

    fun updatePosition(screenX: Float, screenY: Float) {
        activeDrag = activeDrag?.copy(screenX = screenX, screenY = screenY)
    }

    fun endDrag() {
        activeDrag = null
    }

    val isDragging: Boolean get() = activeDrag != null
}

// ─── CompositionLocal ──────────────────────────────────────────────────────────

/** Provides [DragAndDropState] to the entire editor subtree. */
val LocalDragAndDropState = compositionLocalOf { DragAndDropState() }
