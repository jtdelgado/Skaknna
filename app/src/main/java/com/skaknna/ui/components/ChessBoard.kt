package com.skaknna.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skaknna.R
import com.skaknna.ui.theme.*

// ─── Modern Classic Color Definitions ──────────────────────────────────────────

// Chess board square colors: Matte cream and brown (no internal gradients)
val boardLightSquareColor = BoardCream
val boardDarkSquareColor = BoardBrown

// ─── Piece → Drawable resource mapping ────────────────────────────────────────

fun pieceDrawable(piece: Char): Int = when (piece) {
    'K' -> R.drawable.piece_white_king
    'Q' -> R.drawable.piece_white_queen
    'R' -> R.drawable.piece_white_rook
    'B' -> R.drawable.piece_white_bishop
    'N' -> R.drawable.piece_white_knight
    'P' -> R.drawable.piece_white_pawn
    'k' -> R.drawable.piece_black_king
    'q' -> R.drawable.piece_black_queen
    'r' -> R.drawable.piece_black_rook
    'b' -> R.drawable.piece_black_bishop
    'n' -> R.drawable.piece_black_knight
    'p' -> R.drawable.piece_black_pawn
    else -> R.drawable.piece_white_pawn
}

// ─── ChessBoard ────────────────────────────────────────────────────────────────

/**
 * Interactive 8×8 chess board.
 *
 * Board pieces can be dragged (board→board). The palette→board drop is handled
 * entirely within the palette's own gesture handler + EditorScreen.handlePaletteDragEnd(),
 * because Compose gesture detection is non-shareable across composable trees.
 *
 * @param board   8×8 matrix of piece chars (null = empty)
 * @param onDrop  Called when a board piece is released over another cell
 */
@Composable
fun ChessBoard(
    board: Array<Array<Char?>>,
    modifier: Modifier = Modifier,
    eraserMode: Boolean = false,
    onCellTap: (row: Int, col: Int) -> Unit = { _, _ -> },
    onDrop: (source: DragSource, toRow: Int, toCol: Int) -> Unit = { _, _, _ -> },
    /** Called with final screen position when a board piece is released outside the board. */
    onDroppedOutsideBoard: (source: DragSource.FromBoard, screenX: Float, screenY: Float) -> Unit = { _, _, _ -> },
    /** Called with the actual board's window bounds whenever they change. */
    onBoardBoundsChanged: (androidx.compose.ui.geometry.Rect?) -> Unit = {}
) {
    val dndState = LocalDragAndDropState.current
    val activeDrag by remember { derivedStateOf { dndState.activeDrag } }

    var boardBounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }

    // Which cell is the drag currently hovering over?
    val hoveredCell: Pair<Int, Int>? = remember(activeDrag, boardBounds) {
        val drag = activeDrag ?: return@remember null
        val bounds = boardBounds ?: return@remember null
        val cellW = bounds.width / 8f
        val cellH = bounds.height / 8f
        val r = ((drag.screenY - bounds.top) / cellH).toInt()
        val c = ((drag.screenX - bounds.left) / cellW).toInt()
        if (r in 0..7 && c in 0..7) r to c else null
    }

    // BoxWithConstraints lets us measure available space and use the smaller dimension
    // so the board is always square and never overflows its container.
    BoxWithConstraints(modifier = modifier) {
        val squareSize = minOf(maxWidth, maxHeight)

    Column(
        modifier = Modifier
            .size(squareSize)
            .align(Alignment.Center)
            .border(
                width = 1.dp,
                color = OutlineColor,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
            )
            .background(SurfaceDark)
            .padding(6.dp)
            .onGloballyPositioned { coords ->
                boardBounds = coords.boundsInWindow()
                onBoardBoundsChanged(boardBounds)
            }
        // NOTE: No outer pointerInput here. Palette drops are handled in EditorScreen.
    ) {
        for (row in 0..7) {
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                for (col in 0..7) {
                    val isLightSquare = (row + col) % 2 == 0
                    val piece = board[row][col]

                    val isHovered = hoveredCell?.first == row && hoveredCell.second == col

                    val isSourceSquare = activeDrag != null &&
                        activeDrag!!.source is DragSource.FromBoard &&
                        (activeDrag!!.source as DragSource.FromBoard).row == row &&
                        (activeDrag!!.source as DragSource.FromBoard).col == col

                    val pieceAlpha by animateFloatAsState(
                        targetValue = if (isSourceSquare) 0.25f else 1f,
                        label = "pieceAlpha_${row}_$col"
                    )

                    // Eraser mode: red tint on cells with pieces
                    val showEraserOverlay = eraserMode && piece != null

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (isLightSquare) boardLightSquareColor else boardDarkSquareColor
                            )
                            .border(
                                width = 1.dp,
                                color = OutlineColor
                            )
                            .then(
                                when {
                                    // Drop target highlight (during drag)
                                    isHovered && activeDrag != null ->
                                        Modifier.background(GoldGlow)
                                    // Eraser mode: red cell tint on occupied squares
                                    showEraserOverlay ->
                                        Modifier.background(Color.Red.copy(alpha = 0.25f))
                                    else -> Modifier
                                }
                            )
                            .then(
                                // Tap to delete when eraser is active
                                if (eraserMode) {
                                    Modifier.pointerInput(row, col, eraserMode) {
                                        detectTapGestures { onCellTap(row, col) }
                                    }
                                } else {
                                    // Normal drag gesture when NOT in eraser mode
                                    Modifier.pointerInput(row, col) {
                                        detectDragGestures(
                                            onDragStart = { localOffset ->
                                                val cellPiece = board[row][col]
                                                if (cellPiece != null) {
                                                    val bounds = boardBounds ?: return@detectDragGestures
                                                    val cellW = bounds.width / 8f
                                                    val cellH = bounds.height / 8f
                                                    dndState.startDrag(
                                                        piece = cellPiece,
                                                        source = DragSource.FromBoard(row, col),
                                                        screenX = bounds.left + col * cellW + localOffset.x,
                                                        screenY = bounds.top  + row * cellH + localOffset.y
                                                    )
                                                }
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                if (dndState.isDragging) {
                                                    val current = dndState.activeDrag ?: return@detectDragGestures
                                                    dndState.updatePosition(
                                                        screenX = current.screenX + dragAmount.x,
                                                        screenY = current.screenY + dragAmount.y
                                                    )
                                                }
                                            },
                                            onDragEnd = {
                                                val drag = dndState.activeDrag
                                                val bounds = boardBounds
                                                try {
                                                    if (drag != null && bounds != null) {
                                                        val cellW = bounds.width / 8f
                                                        val cellH = bounds.height / 8f
                                                        val dropRow = ((drag.screenY - bounds.top) / cellH).toInt()
                                                        val dropCol = ((drag.screenX - bounds.left) / cellW).toInt()
                                                        if (dropRow in 0..7 && dropCol in 0..7) {
                                                            onDrop(drag.source, dropRow, dropCol)
                                                        } else if (drag.source is DragSource.FromBoard) {
                                                            // Released outside the board — notify parent (e.g. delete zone)
                                                            onDroppedOutsideBoard(
                                                                drag.source,
                                                                drag.screenX,
                                                                drag.screenY
                                                            )
                                                        }
                                                    }
                                                } finally {
                                                    dndState.endDrag()
                                                }
                                            },
                                            onDragCancel = { dndState.endDrag() }
                                        )
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (piece != null) {
                            Image(
                                painter = painterResource(id = pieceDrawable(piece)),
                                contentDescription = stringResource(id = R.string.piece_generic_description, piece.toString()),
                                modifier = Modifier
                                    .fillMaxSize(0.85f)
                                    .graphicsLayer(alpha = pieceAlpha)
                            )
                        }

                        // Eraser mode: X overlay on pieces
                        if (showEraserOverlay) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(0.55f)
                                    .background(
                                        Color.Red.copy(alpha = 0.85f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✕",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Drop indicator dot on empty cell (during drag)
                        if (isHovered && activeDrag != null && piece == null) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(PrimaryGold, shape = CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
    } // BoxWithConstraints
}
