package com.skaknna.ui.components

// ─── Piece limits ──────────────────────────────────────────────────────────────
// Maximum pieces of each type allowed on the board at any time (editor mode)
val PIECE_MAX_COUNTS: Map<Char, Int> = mapOf(
    'K' to 1,  'k' to 1,  // Kings: exactly 1 each
    'Q' to 9,  'q' to 9,  // Queens: 1 + 8 possible promotions
    'R' to 10, 'r' to 10, // Rooks
    'B' to 10, 'b' to 10, // Bishops
    'N' to 10, 'n' to 10, // Knights
    'P' to 8,  'p' to 8   // Pawns: max 8 per side
)

// ─── FEN ↔ Board matrix ────────────────────────────────────────────────────────

/**
 * Converts the piece-placement part of a FEN string into an 8×8 matrix.
 * board[0][0] = a8 (top-left from White's perspective = rank 8, file a)
 * board[7][7] = h1 (bottom-right = rank 1, file h)
 * null means empty square.
 */
fun fenToBoard(fen: String): Array<Array<Char?>> {
    val board = Array(8) { arrayOfNulls<Char>(8) }
    val ranks = fen.split(" ")[0].split("/")
    for (rankIdx in ranks.indices) {
        if (rankIdx >= 8) break
        var fileIdx = 0
        for (ch in ranks[rankIdx]) {
            if (fileIdx >= 8) break
            if (ch.isDigit()) {
                fileIdx += ch.digitToInt()
            } else {
                board[rankIdx][fileIdx] = ch
                fileIdx++
            }
        }
    }
    return board
}

/**
 * Converts an 8×8 board matrix back into a FEN piece-placement string.
 * Appends " w KQkq - 0 1" as placeholder for the rest of the FEN.
 */
fun boardToFen(board: Array<Array<Char?>>): String {
    val sb = StringBuilder()
    for (row in 0..7) {
        var empty = 0
        for (col in 0..7) {
            val piece = board[row][col]
            if (piece == null) {
                empty++
            } else {
                if (empty > 0) { sb.append(empty); empty = 0 }
                sb.append(piece)
            }
        }
        if (empty > 0) sb.append(empty)
        if (row < 7) sb.append('/')
    }
    sb.append(" w KQkq - 0 1")
    return sb.toString()
}

// ─── Piece count helpers ────────────────────────────────────────────────────────

/** Counts how many of each piece type are currently on the board. */
fun countPieces(board: Array<Array<Char?>>): Map<Char, Int> {
    val counts = mutableMapOf<Char, Int>()
    for (row in board) for (cell in row) if (cell != null) {
        counts[cell] = (counts[cell] ?: 0) + 1
    }
    return counts
}

/**
 * Returns true if placing [piece] onto the board is allowed (not exceeding piece limits).
 * [excludeRow] / [excludeCol]: if the piece is being *moved* from the board itself,
 * pass the origin cell so we don't double-count it.
 */
fun canPlacePiece(
    board: Array<Array<Char?>>,
    piece: Char,
    excludeRow: Int = -1,
    excludeCol: Int = -1
): Boolean {
    var currentCount = 0
    for (r in 0..7) for (c in 0..7) {
        if (r == excludeRow && c == excludeCol) continue // skip the source square
        if (board[r][c] == piece) currentCount++
    }
    val max = PIECE_MAX_COUNTS[piece] ?: 1
    return currentCount < max
}

// ─── Validation ────────────────────────────────────────────────────────────────

/**
 * Sealed class to represent board validation warnings.
 * Each warning type corresponds to a translatable string resource.
 */
sealed class BoardWarning {
    object WhiteNoKing : BoardWarning()
    object WhiteMultipleKings : BoardWarning()
    object BlackNoKing : BoardWarning()
    object BlackMultipleKings : BoardWarning()
    object WhitePawnRank8 : BoardWarning()
    object WhitePawnRank1 : BoardWarning()
    object BlackPawnRank8 : BoardWarning()
    object BlackPawnRank1 : BoardWarning()
    data class WhitePawnsCount(val count: Int) : BoardWarning()
    data class BlackPawnsCount(val count: Int) : BoardWarning()
}

data class BoardValidation(
    val isValid: Boolean,
    val warnings: List<BoardWarning>
)

/**
 * Validates a board position and returns a list of rule violations.
 * The editor allows illegal positions but warns the user.
 */
fun validateBoard(board: Array<Array<Char?>>): BoardValidation {
    val counts = countPieces(board)
    val warnings = mutableListOf<BoardWarning>()

    // King counts
    val whiteKings = counts['K'] ?: 0
    val blackKings = counts['k'] ?: 0
    if (whiteKings == 0) warnings.add(BoardWarning.WhiteNoKing)
    if (whiteKings > 1) warnings.add(BoardWarning.WhiteMultipleKings)
    if (blackKings == 0) warnings.add(BoardWarning.BlackNoKing)
    if (blackKings > 1) warnings.add(BoardWarning.BlackMultipleKings)

    // Pawns on promotion ranks (rows 0 and 7)
    var whitePawnOnRank8 = false
    var blackPawnOnRank1 = false
    var whitePawnOnRank1 = false
    var blackPawnOnRank8 = false
    for (col in 0..7) {
        if (board[0][col] == 'P') whitePawnOnRank8 = true
        if (board[7][col] == 'p') blackPawnOnRank1 = true
        if (board[0][col] == 'p') blackPawnOnRank8 = true
        if (board[7][col] == 'P') whitePawnOnRank1 = true
    }
    if (whitePawnOnRank8) warnings.add(BoardWarning.WhitePawnRank8)
    if (whitePawnOnRank1) warnings.add(BoardWarning.WhitePawnRank1)
    if (blackPawnOnRank8) warnings.add(BoardWarning.BlackPawnRank8)
    if (blackPawnOnRank1) warnings.add(BoardWarning.BlackPawnRank1)

    // Too many pawns
    val whitePawns = counts['P'] ?: 0
    val blackPawns = counts['p'] ?: 0
    if (whitePawns > 8) warnings.add(BoardWarning.WhitePawnsCount(whitePawns))
    if (blackPawns > 8) warnings.add(BoardWarning.BlackPawnsCount(blackPawns))

    return BoardValidation(isValid = warnings.isEmpty(), warnings = warnings.distinct())
}
