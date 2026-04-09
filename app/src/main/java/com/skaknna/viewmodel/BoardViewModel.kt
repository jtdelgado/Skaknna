package com.skaknna.viewmodel

import androidx.lifecycle.ViewModel
import com.skaknna.ui.components.BoardValidation
import com.skaknna.ui.components.boardToFen
import com.skaknna.ui.components.canPlacePiece
import com.skaknna.ui.components.fenToBoard
import com.skaknna.ui.components.validateBoard
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.skaknna.data.model.Board
import com.skaknna.data.repository.BoardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BoardViewModel(private val repository: BoardRepository) : ViewModel() {

    // ─── Saved Boards (from local DB) ─────────────────────────────────────────
    val allBoards: StateFlow<List<Board>> = repository.allBoards
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ─── FEN string (source of truth) ─────────────────────────────────────────
    private val _fen = MutableStateFlow("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
    val fen: StateFlow<String> = _fen.asStateFlow()

    // ─── Board matrix (derived from FEN) ──────────────────────────────────────
    private val _board = MutableStateFlow(fenToBoard(_fen.value))
    val board: StateFlow<Array<Array<Char?>>> = _board.asStateFlow()

    // ─── Validation state ──────────────────────────────────────────────────────
    private val _validation = MutableStateFlow(validateBoard(_board.value))
    val validation: StateFlow<BoardValidation> = _validation.asStateFlow()

    // ─── FEN text input (can be typed directly) ────────────────────────────────
    fun updateFen(newFen: String) {
        _fen.value = newFen
        val parsed = fenToBoard(newFen)
        _board.value = parsed
        _validation.value = validateBoard(parsed)
    }

    // ─── Board mutation operations ─────────────────────────────────────────────

    /**
     * Places [piece] on [row],[col], replacing whatever was there.
     * Returns false if the piece limit would be exceeded.
     */
    fun placePiece(row: Int, col: Int, piece: Char): Boolean {
        val current = _board.value
        // If same piece is already there — nothing to do
        if (current[row][col] == piece) return true

        if (!canPlacePiece(current, piece)) return false

        val newBoard = current.deepCopy()
        newBoard[row][col] = piece
        commitBoard(newBoard)
        return true
    }

    /**
     * Moves a piece from [fromRow],[fromCol] to [toRow],[toCol].
     * Replaces whatever is at the destination.
     * Returns false if the move is not permitted (e.g. piece limit of the piece type).
     */
    fun movePiece(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Boolean {
        val current = _board.value
        val piece = current[fromRow][fromCol] ?: return false

        // Moving within the board: we exclude the origin square from count
        if (!canPlacePiece(current, piece, excludeRow = fromRow, excludeCol = fromCol)) return false

        val newBoard = current.deepCopy()
        newBoard[fromRow][fromCol] = null
        newBoard[toRow][toCol] = piece
        commitBoard(newBoard)
        return true
    }

    /** Removes the piece on [row],[col] (if any). */
    fun removePiece(row: Int, col: Int) {
        val current = _board.value
        if (current[row][col] == null) return
        val newBoard = current.deepCopy()
        newBoard[row][col] = null
        commitBoard(newBoard)
    }

    /** Resets the board to the standard starting position. */
    fun resetToStartPosition() {
        updateFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
    }

    /** Clears all pieces from the board. */
    fun clearBoard() {
        updateFen("8/8/8/8/8/8/8/8 w - - 0 1")
    }

    // ─── Persistence ───────────────────────────────────────────────────────────

    fun saveBoard(name: String) {
        viewModelScope.launch {
            val board = Board(
                name = name,
                fen = _fen.value,
                // userId would be set here if we had Firebase Auth implemented
                userId = null
            )
            repository.saveBoard(board)
        }
    }

    // ─── Private helpers ───────────────────────────────────────────────────────

    private fun commitBoard(newBoard: Array<Array<Char?>>) {
        _board.value = newBoard
        _fen.value = boardToFen(newBoard)
        _validation.value = validateBoard(newBoard)
    }

    private fun Array<Array<Char?>>.deepCopy(): Array<Array<Char?>> =
        Array(size) { r -> Array(this[r].size) { c -> this[r][c] } }
}

class BoardViewModelFactory(private val repository: BoardRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BoardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BoardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
