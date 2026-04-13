package com.skaknna.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.skaknna.ui.components.BoardValidation
import com.skaknna.ui.components.boardToFen
import com.skaknna.ui.components.canPlacePiece
import com.skaknna.ui.components.fenToBoard
import com.skaknna.ui.components.validateBoard
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.skaknna.chess.StockfishManager
import com.skaknna.data.model.Board
import com.skaknna.data.repository.BoardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.delay

class BoardViewModel(
    private val repository: BoardRepository,
    private val settingsViewModel: SettingsViewModel,
    context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "BoardViewModel"
    }

    // ─── Stockfish Manager ──────────────────────────────────────────────────────
    private val stockfishManager = StockfishManager(context)
    private var stockfishInitialized = false
    private var analysisJob: Job? = null
    private val analysisMutex = Mutex()
    private val MAX_INIT_ATTEMPTS = 3
    private var inAnalysisScreen = false  // Flag to suppress auto-analysis during FEN updates

    // ─── Analysis State ────────────────────────────────────────────────────────
    private val _bestMove = MutableStateFlow("")
    val bestMove: StateFlow<String> = _bestMove.asStateFlow()

    private val _evaluation = MutableStateFlow(0f)
    val evaluation: StateFlow<Float> = _evaluation.asStateFlow()

    private val _analysisLine = MutableStateFlow("")
    val analysisLine: StateFlow<String> = _analysisLine.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

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

    init {
        // Auto-trigger analysis when FEN changes (with debounce)
        // ONLY if Stockfish has been manually initialized AND we're in AnalysisScreen
        // (prevents double-analysis when switching between boards)
        viewModelScope.launch {
            _fen
                .debounce(200) // Wait 200ms for rapid FEN changes to settle
                .collectLatest { fen ->
                    if (stockfishInitialized && fen.isNotBlank() && inAnalysisScreen) {
                        triggerAnalysis(fen)
                    }
                }
        }

        // Monitor changes in analysis depth setting and re-analyze if it changes
        // ONLY if Stockfish has been manually initialized
        viewModelScope.launch {
            settingsViewModel.analysisDepth
                .debounce(200) // Avoid rapid re-analysis while slider is moving
                .collectLatest { newDepth ->
                    if (stockfishInitialized && _fen.value.isNotBlank()) {
                        Log.d(TAG, "Analysis depth changed to $newDepth, re-analyzing current position...")
                        triggerAnalysis(_fen.value)
                    }
                }
        }
    }


    // ─── Screen Navigation ──────────────────────────────────────────────────────
    fun enterAnalysisScreen() {
        inAnalysisScreen = true
    }

    fun exitAnalysisScreen() {
        inAnalysisScreen = false
    }

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

    fun renameBoard(board: Board, newName: String) {
        viewModelScope.launch {
            val updatedBoard = board.copy(
                name = newName,
                updatedAt = System.currentTimeMillis(),
                isSynced = false // Needs to sync the new name
            )
            repository.saveBoard(updatedBoard)
        }
    }

    fun deleteBoard(board: Board) {
        viewModelScope.launch {
            repository.deleteBoard(board.id, board.userId)
        }
    }

    // ─── Stockfish Analysis ────────────────────────────────────────────────────

    /**
     * Manually ensures Stockfish is initialized.
     * Useful when entering Analysis Screen to guarantee engine readiness.
     */
    fun ensureStockfishInitialized() {
        viewModelScope.launch {
            if (stockfishInitialized) {
                Log.d(TAG, "Stockfish already initialized")
                return@launch
            }
            
            Log.d(TAG, "=== Ensuring Stockfish Initialization ===")
            var attempts = 0
            while (attempts < MAX_INIT_ATTEMPTS && !stockfishInitialized) {
                attempts++
                Log.d(TAG, "Attempting to initialize Stockfish (attempt $attempts/$MAX_INIT_ATTEMPTS)...")
                
                val error = stockfishManager.startEngine()
                if (error != null) {
                    Log.w(TAG, "WARNING: Failed to initialize Stockfish: $error")
                    if (attempts < MAX_INIT_ATTEMPTS) {
                        val delayMs = 500L * (1L shl (attempts - 1))
                        Log.d(TAG, "Retrying after ${delayMs}ms...")
                        delay(delayMs)
                    }
                } else {
                    stockfishInitialized = true
                    Log.d(TAG, "OK: Stockfish initialized successfully")
                    break
                }
            }
            
            if (!stockfishInitialized) {
                Log.e(TAG, "ERROR: Could not initialize Stockfish for analysis")
            }
        }
    }

    /**
     * Manually triggers analysis of the current position.
     * Useful when entering Analysis Screen to ensure analysis happens.
     * Will attempt to re-initialize Stockfish if it's not ready.
     */
    fun analyzeCurrentPosition() {
        if (_fen.value.isBlank()) {
            Log.w(TAG, "Cannot analyze: invalid FEN")
            return
        }
        
        if (!stockfishInitialized) {
            Log.d(TAG, "Stockfish not initialized, attempting to initialize for analysis...")
            ensureStockfishInitialized()
            // Give it time to initialize, then trigger analysis
            viewModelScope.launch {
                kotlinx.coroutines.delay(800)
                if (stockfishInitialized) {
                    Log.d(TAG, "Stockfish ready, starting analysis...")
                    triggerAnalysis(_fen.value)
                } else {
                    Log.e(TAG, "ERROR: Could not initialize Stockfish for analysis after retry")
                }
            }
        } else {
            triggerAnalysis(_fen.value)
        }
    }

    /**
     * Triggers async analysis of the current position using Stockfish.
     * The analysis depth is taken from SettingsViewModel.
     * Only one analysis runs at a time (protected by mutex).
     */
    private fun triggerAnalysis(fen: String) {
        // Cancel the current analysis job before starting a new one
        val prevJob = analysisJob
        analysisJob = null
        prevJob?.cancel()
        
        analysisJob = viewModelScope.launch {
            analysisMutex.withLock {
                stockfishManager.stopAnalysis() // Tell stockfish to abort previous search if busy
                try {
                    // Check if this job was cancelled while waiting for the mutex
                    ensureActive()
                    
                    _isAnalyzing.value = true
                    val depth = settingsViewModel.analysisDepth.value

                    Log.d(TAG, "=== Triggering Analysis ===")
                    Log.d(TAG, "FEN: $fen")
                    Log.d(TAG, "Depth: $depth")

                    // Only analyze if depth > 0
                    if (depth <= 0) {
                        Log.w(TAG, "WARNING: Depth is 0 or negative, skipping analysis")
                        _bestMove.value = ""
                        _evaluation.value = 0f
                        _analysisLine.value = ""
                        return@withLock
                    }

                    Log.d(TAG, "Calling stockfishManager.analyzePosition()...")
                    val result = stockfishManager.analyzePosition(fen, depth)
                    
                    // Check cancellation again after analysis completes
                    ensureActive()
                    
                    if (result != null) {
                        _bestMove.value = result.bestMove
                        _evaluation.value = result.evaluation
                        _analysisLine.value = result.principalVariation
                        Log.d(TAG, "OK: Analysis complete:")
                        Log.d(TAG, "  Best Move: ${result.bestMove}")
                        Log.d(TAG, "  Evaluation: ${result.evaluation}")
                        Log.d(TAG, "  Line: ${result.principalVariation}")
                    } else {
                        Log.d(TAG, "INFO: Analysis returned null result (was stopped or cancelled)")
                        _bestMove.value = ""
                        _evaluation.value = 0f
                        _analysisLine.value = ""
                        
                        // Check if the engine actually crashed
                        if (!stockfishManager.isEngineReady) {
                            Log.w(TAG, "Engine crashed. Forcing restart...")
                            stockfishInitialized = false // Reset our local flag
                            ensureStockfishInitialized() // Automatically reboot
                            
                            // Re-trigger the analysis once it's back up
                            viewModelScope.launch {
                                kotlinx.coroutines.delay(1000)
                                if (stockfishInitialized) {
                                    triggerAnalysis(fen)
                                }
                            }
                        }
                    }
                } catch (e: CancellationException) {
                    Log.d(TAG, "INFO: Analysis was cancelled")
                    throw e // Re-throw to properly mark the coroutine as cancelled
                } catch (e: Exception) {
                    Log.e(TAG, "ERROR: Error during analysis: ${e.message}", e)
                    // If analysis fails, mark Stockfish as not initialized for retry on next attempt
                    stockfishInitialized = false
                    _bestMove.value = ""
                    _evaluation.value = 0f
                    _analysisLine.value = ""
                } finally {
                    _isAnalyzing.value = false
                }
            }
        }
    }

    /**
     * Manually stop the current analysis.
     */
    fun stopAnalysis() {
        viewModelScope.launch {
            stockfishManager.stopAnalysis()
            _isAnalyzing.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up Stockfish process
        viewModelScope.launch {
            stockfishManager.shutdown()
            Log.d(TAG, "Stockfish manager shut down")
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

class BoardViewModelFactory(
    private val repository: BoardRepository,
    private val settingsViewModel: SettingsViewModel,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BoardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BoardViewModel(repository, settingsViewModel, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
