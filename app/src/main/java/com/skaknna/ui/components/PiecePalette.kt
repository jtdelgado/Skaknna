package com.skaknna.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skaknna.R
import com.skaknna.ui.theme.*

/**
 * A row of chess pieces for one side. Each piece can be:
 * - Dragged onto the board
 * - Has a "MAX" badge when its count limit is reached
 *
 * Note: the palette does NOT call [DragAndDropState.endDrag] on its own drag end,
 * because the board's [detectDragGestures] needs to be the one finishing the gesture
 * and invoking [onDrop]. The parent EditorScreen detects when the drag ends outside
 * the board via [onPieceDragEnd].
 */
@Composable
fun PiecePalette(
    isBlack: Boolean,
    board: Array<Array<Char?>>,
    modifier: Modifier = Modifier,
    onPieceDragEnd: () -> Unit = {}
) {
    val pieces: List<Pair<Char, String>> = if (isBlack) {
        listOf('k' to "Rey", 'q' to "Reina", 'r' to "Torre",
               'b' to "Alfil", 'n' to "Caballo", 'p' to "Peón")
    } else {
        listOf('K' to "Rey", 'Q' to "Reina", 'R' to "Torre",
               'B' to "Alfil", 'N' to "Caballo", 'P' to "Peón")
    }

    val dndState = LocalDragAndDropState.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        pieces.forEach { (piece, _) ->
            val atLimit = !canPlacePiece(board, piece)
            val alpha by animateFloatAsState(
                targetValue = if (atLimit) 0.35f else 1f,
                label = "paletteAlpha_$piece"
            )

            var tileBounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(WoodMedium)
                    .border(
                        width = 1.5.dp,
                        color = when {
                            atLimit -> WoodDark
                            else -> WoodLight.copy(alpha = 0.5f)
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    .graphicsLayer(alpha = alpha)
                    .onGloballyPositioned { coords ->
                        tileBounds = coords.boundsInWindow()
                    }
                    .pointerInput(piece, atLimit) {
                        if (atLimit) return@pointerInput
                        detectDragGestures(
                            onDragStart = { localOffset ->
                                val bounds = tileBounds ?: return@detectDragGestures
                                dndState.startDrag(
                                    piece = piece,
                                    source = DragSource.FromPalette(isBlack),
                                    screenX = bounds.left + localOffset.x,
                                    screenY = bounds.top  + localOffset.y
                                )
                            },
                            onDrag = { change, dragAmount ->
                                // FIX: accumulate using delta so position stays correct
                                // when finger moves far beyond the tile boundaries.
                                change.consume()
                                val current = dndState.activeDrag ?: return@detectDragGestures
                                dndState.updatePosition(
                                    screenX = current.screenX + dragAmount.x,
                                    screenY = current.screenY + dragAmount.y
                                )
                            },
                            onDragEnd = {
                                // The palette gesture owns the event end.
                                // Placement logic + endDrag() happen in EditorScreen.handlePaletteDragEnd().
                                onPieceDragEnd()
                            },
                            onDragCancel = {
                                dndState.endDrag()
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = pieceDrawable(piece)),
                    contentDescription = if (isBlack) stringResource(id = R.string.piece_black_description, piece.toString()) else stringResource(id = R.string.piece_white_description, piece.toString()),
                    modifier = Modifier.fillMaxSize(0.80f)
                )

                if (atLimit) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .background(WoodDark.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 2.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.editor_piece_limit_badge),
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldenYellow
                        )
                    }
                }
            }
        }
    }
}
