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
import com.skaknna.data.model.SyncState
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
import com.skaknna.R

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errorResId: Int) : ValidationResult()
}

class BoardViewModel(
    private val repository: BoardRepository,
    private val settingsViewModel: SettingsViewModel,
    context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "BoardViewModel"
    }

    private val stockfishManager = StockfishManager(context)
    private var stockfishInitialized = false
    private var analysisJob: Job? = null
    private val analysisMutex = Mutex()
    private val MAX_INIT_ATTEMPTS = 3
    private var inAnalysisScreen = false

    private val _bestMove = MutableStateFlow("")
    val bestMove: StateFlow<String> = _bestMove.asStateFlow()

    private val _evaluation = MutableStateFlow(0f)
    val evaluation: StateFlow<Float> = _evaluation.asStateFlow()

    private val _analysisLine = MutableStateFlow("")
    val analysisLine: StateFlow<String> = _analysisLine.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    val allBoards: StateFlow<List<Board>> = repository.allBoards
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _fen = MutableStateFlow("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
    val fen: StateFlow<String> = _fen.asStateFlow()

    private val _board = MutableStateFlow(fenToBoard(_fen.value))
    val board: StateFlow<Array<Array<Char?>>> = _board.asStateFlow()

    private val _validation = MutableStateFlow(validateBoard(_board.value))
    val validation: StateFlow<BoardValidation> = _validation.asStateFlow()

    private val _showSaveDialog = MutableStateFlow(false)
    val showSaveDialog: StateFlow<Boolean> = _showSaveDialog.asStateFlow()

    private val _saveErrorMessage = MutableStateFlow<Int?>(null)
    val saveErrorMessage: StateFlow<Int?> = _saveErrorMessage.asStateFlow()

    fun isFenLegal(fen: String, turn: String): ValidationResult {
        val parts = fen.split(" ").toMutableList()
        if (parts.size == 1) {
            parts.addAll(listOf(turn, "KQkq", "-", "0", "1"))
        } else {
            parts[1] = turn
        }
        val fullFen = parts.joinToString(" ")

        val boardPart = parts[0]
        
        val whiteKings = boardPart.count { it == 'K' }
        val blackKings = boardPart.count { it == 'k' }
        if (whiteKings != 1 || blackKings != 1) {
            return ValidationResult.Invalid(R.string.error_missing_kings)
        }

        // Chesslib strict validation
        val board = com.github.bhlangonijr.chesslib.Board()
        try {
            board.loadFromFen(fullFen)
            
            val inactiveSide = if (board.sideToMove == com.github.bhlangonijr.chesslib.Side.WHITE) 
                com.github.bhlangonijr.chesslib.Side.BLACK else com.github.bhlangonijr.chesslib.Side.WHITE
                
            val inactiveKingSquare = board.getKingSquare(inactiveSide)
            if (inactiveKingSquare != com.github.bhlangonijr.chesslib.Square.NONE) {
                if (board.squareAttackedBy(inactiveKingSquare, board.sideToMove) != 0L) {
                    return ValidationResult.Invalid(R.string.error_chesslib_illegal)
                }
            }
        } catch (e: Exception) {
            // Any parsing exception from chesslib means the FEN is fundamentally invalid
            return ValidationResult.Invalid(R.string.error_invalid_fen_regex)
        }

        // Inventory validation (Promotion rule)
        
        // Count pieces
        val whitePawns = boardPart.count { it == 'P' }
        val whiteKnights = boardPart.count { it == 'N' }
        val whiteBishops = boardPart.count { it == 'B' }
        val whiteRooks = boardPart.count { it == 'R' }
        val whiteQueens = boardPart.count { it == 'Q' }

        val blackPawns = boardPart.count { it == 'p' }
        val blackKnights = boardPart.count { it == 'n' }
        val blackBishops = boardPart.count { it == 'b' }
        val blackRooks = boardPart.count { it == 'r' }
        val blackQueens = boardPart.count { it == 'q' }

        // White inventory check
        val missingWhitePawns = 8 - whitePawns
        val extraWhitePieces = maxOf(0, whiteQueens - 1) +
                maxOf(0, whiteRooks - 2) +
                maxOf(0, whiteBishops - 2) +
                maxOf(0, whiteKnights - 2)

        if (extraWhitePieces > missingWhitePawns) {
            return ValidationResult.Invalid(R.string.error_impossible_inventory)
        }

        // Black inventory check
        val missingBlackPawns = 8 - blackPawns
        val extraBlackPieces = maxOf(0, blackQueens - 1) +
                maxOf(0, blackRooks - 2) +
                maxOf(0, blackBishops - 2) +
                maxOf(0, blackKnights - 2)

        if (extraBlackPieces > missingBlackPawns) {
            return ValidationResult.Invalid(R.string.error_impossible_inventory)
        }

        return ValidationResult.Valid
    }

    fun onSaveIconClicked() {
        _showSaveDialog.value = true
    }

    fun onSaveConfirmClicked(turn: String): Boolean {
        val result = isFenLegal(_fen.value, turn)
        if (result is ValidationResult.Valid) {
            val parts = _fen.value.split(" ").toMutableList()
            if (parts.isNotEmpty()) {
                if (parts.size == 1) {
                    parts.addAll(listOf(turn, "KQkq", "-", "0", "1"))
                } else {
                    parts[1] = turn
                }
                val newFen = parts.joinToString(" ")
                if (newFen != _fen.value) {
                    updateFen(newFen)
                }
            }
            _showSaveDialog.value = false
            return true
        } else if (result is ValidationResult.Invalid) {
            _saveErrorMessage.value = result.errorResId
            return false
        }
        return false
    }

    fun dismissSaveDialog() {
        _showSaveDialog.value = false
    }

    fun clearSaveErrorMessage() {
        _saveErrorMessage.value = null
    }

    init {
        viewModelScope.launch {
            _fen
                .debounce(200)
                .collectLatest { fen ->
                    if (stockfishInitialized && fen.isNotBlank() && inAnalysisScreen) {
                        triggerAnalysis(fen)
                    }
                }
        }

        viewModelScope.launch {
            settingsViewModel.analysisLevel
                .debounce(200)
                .collectLatest { newLevel ->
                    if (stockfishInitialized && _fen.value.isNotBlank()) {
                        Log.d(TAG, "Analysis depth changed to ${newLevel.depth}, re-analyzing...")
                        triggerAnalysis(_fen.value)
                    }
                }
        }
    }

    fun enterAnalysisScreen() { inAnalysisScreen = true }
    fun exitAnalysisScreen() { inAnalysisScreen = false }

    fun updateFen(newFen: String) {
        _fen.value = newFen
        val parsed = fenToBoard(newFen)
        _board.value = parsed
        _validation.value = validateBoard(parsed)
    }

    fun placePiece(row: Int, col: Int, piece: Char): Boolean {
        val current = _board.value
        if (current[row][col] == piece) return true
        if (!canPlacePiece(current, piece)) return false
        val newBoard = current.deepCopy()
        newBoard[row][col] = piece
        commitBoard(newBoard)
        return true
    }

    fun movePiece(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Boolean {
        val current = _board.value
        val piece = current[fromRow][fromCol] ?: return false
        if (!canPlacePiece(current, piece, excludeRow = fromRow, excludeCol = fromCol)) return false
        val newBoard = current.deepCopy()
        newBoard[fromRow][fromCol] = null
        newBoard[toRow][toCol] = piece
        commitBoard(newBoard)
        return true
    }

    fun removePiece(row: Int, col: Int) {
        val current = _board.value
        if (current[row][col] == null) return
        val newBoard = current.deepCopy()
        newBoard[row][col] = null
        commitBoard(newBoard)
    }

    fun resetToStartPosition() {
        updateFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
    }

    fun clearBoard() {
        updateFen("8/8/8/8/8/8/8/8 w - - 0 1")
    }

    fun saveBoard(name: String, userId: String? = null) {
        viewModelScope.launch {
            val board = Board(name = name, fen = _fen.value, userId = userId)
            repository.saveBoard(board)
            Log.d(TAG, "Board '$name' saved locally. userId=$userId")
        }
    }

    fun renameBoard(board: Board, newName: String) {
        viewModelScope.launch {
            repository.saveBoard(board.copy(
                name = newName,
                updatedAt = System.currentTimeMillis(),
                isSynced = false
            ))
        }
    }

    fun deleteBoard(board: Board) {
        viewModelScope.launch {
            repository.deleteBoard(board.id, board.userId)
        }
    }

    fun syncWithCloud(userId: String) {
        viewModelScope.launch {
            if (_syncState.value is SyncState.Syncing) return@launch
            _syncState.value = SyncState.Syncing
            Log.d(TAG, "Cloud sync started for user=$userId")
            try {
                val (uploaded, downloaded) = repository.syncWithCloud(userId)
                Log.d(TAG, "Sync complete: uploaded=$uploaded, downloaded=$downloaded")
                _syncState.value = SyncState.Success(uploaded, downloaded)
            } catch (e: Exception) {
                Log.e(TAG, "Sync failed: ${e.message}", e)
                _syncState.value = SyncState.Error(e.message ?: "Unknown sync error")
            }
        }
    }

    fun resetSyncState() {
        _syncState.value = SyncState.Idle
    }

    fun ensureStockfishInitialized() {
        viewModelScope.launch {
            if (stockfishInitialized) return@launch
            var attempts = 0
            while (attempts < MAX_INIT_ATTEMPTS && !stockfishInitialized) {
                attempts++
                val error = stockfishManager.startEngine()
                if (error != null) {
                    Log.w(TAG, "Stockfish init failed (attempt $attempts): $error")
                    if (attempts < MAX_INIT_ATTEMPTS) delay(500L * (1L shl (attempts - 1)))
                } else {
                    stockfishInitialized = true
                    Log.d(TAG, "Stockfish initialized")
                    break
                }
            }
            if (!stockfishInitialized) Log.e(TAG, "Could not initialize Stockfish")
        }
    }

    fun analyzeCurrentPosition() {
        if (_fen.value.isBlank()) return
        if (!stockfishInitialized) {
            ensureStockfishInitialized()
            viewModelScope.launch {
                kotlinx.coroutines.delay(800)
                if (stockfishInitialized) triggerAnalysis(_fen.value)
            }
        } else {
            triggerAnalysis(_fen.value)
        }
    }

    private fun triggerAnalysis(fen: String) {
        val prevJob = analysisJob
        analysisJob = null
        prevJob?.cancel()

        analysisJob = viewModelScope.launch {
            analysisMutex.withLock {
                stockfishManager.stopAnalysis()
                try {
                    ensureActive()
                    _isAnalyzing.value = true
                    val depth = settingsViewModel.analysisLevel.value.depth

                    if (depth <= 0) {
                        _bestMove.value = ""
                        _evaluation.value = 0f
                        _analysisLine.value = ""
                        return@withLock
                    }

                    var finalResultReceived = false
                    try {
                        stockfishManager.analyzePosition(fen, depth).collect { update ->
                            ensureActive()
                            _evaluation.value = update.evaluation
                            if (update.principalVariation.isNotBlank()) {
                                _analysisLine.value = update.principalVariation
                            }
                            if (update.bestMove.isNotBlank()) {
                                _bestMove.value = update.bestMove
                                finalResultReceived = true
                                Log.d(TAG, "Analysis complete: bestMove=${update.bestMove} eval=${update.evaluation}")
                            }
                        }
                    } catch (e: Exception) {
                        if (e !is CancellationException) {
                            Log.e(TAG, "Error in analysis flow: ${e.message}", e)
                        } else {
                            throw e
                        }
                    }

                    if (!finalResultReceived && !stockfishManager.isEngineReady) {
                        _bestMove.value = ""
                        _evaluation.value = 0f
                        _analysisLine.value = ""
                        Log.w(TAG, "Engine crashed, restarting...")
                        stockfishInitialized = false
                        ensureStockfishInitialized()
                        viewModelScope.launch {
                            kotlinx.coroutines.delay(1000)
                            if (stockfishInitialized) triggerAnalysis(fen)
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Analysis error: ${e.message}", e)
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

    fun stopAnalysis() {
        viewModelScope.launch {
            stockfishManager.stopAnalysis()
            _isAnalyzing.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch { stockfishManager.shutdown() }
    }

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
