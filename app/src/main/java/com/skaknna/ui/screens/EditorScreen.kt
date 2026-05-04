package com.skaknna.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import com.skaknna.R
import com.skaknna.ui.components.*
import com.skaknna.ui.theme.*
import com.skaknna.viewmodel.BoardViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: BoardViewModel,
    onSaveBoard: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val fen by viewModel.fen.collectAsState()
    val board by viewModel.board.collectAsState()
    val validation by viewModel.validation.collectAsState()
    val showSaveDialog by viewModel.showSaveDialog.collectAsState()
    val saveErrorMessage by viewModel.saveErrorMessage.collectAsState()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    var boardName by remember { mutableStateOf("") }
    var selectedTurn by remember { mutableStateOf("w") }

    // ─── DnD State ────────────────────────────────────────────────────────────
    val dndState = remember { DragAndDropState() }

    var boardBoundsInWindow by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }

    // ─── Delete zone state ─────────────────────────────────────────────────────
    // Floating trash button that appears when dragging a board piece.
    var deleteZoneBounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }

    val activeDrag = dndState.activeDrag
    val isDraggingFromBoard = activeDrag?.source is DragSource.FromBoard

    // Is the finger currently over the delete zone?
    val isHoveringDelete = remember(activeDrag, deleteZoneBounds) {
        val drag = activeDrag ?: return@remember false
        val zone = deleteZoneBounds ?: return@remember false
        drag.screenX in zone.left..zone.right && drag.screenY in zone.top..zone.bottom
    }

    // ─── Save dialog ──────────────────────────────────────────────────────────
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSaveDialog() },
            title = {
                Text(
                    stringResource(id = R.string.editor_save_dialog_title),
                    color = PrimaryGold,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = boardName,
                        onValueChange = { boardName = it },
                        label = { Text(stringResource(id = R.string.editor_game_name_label)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGold,
                            unfocusedBorderColor = OutlineColor,
                            focusedTextColor = WarmWhite,
                            unfocusedTextColor = WarmWhite,
                            cursorColor = PrimaryGold,
                            focusedLabelColor = PrimaryGold,
                            unfocusedLabelColor = WarmWhite.copy(alpha = 0.7f)
                        )
                    )

                    Text(
                        text = stringResource(id = R.string.editor_whose_turn_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmWhite
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedTurn = "w" }) {
                            RadioButton(
                                selected = selectedTurn == "w",
                                onClick = { selectedTurn = "w" },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = PrimaryGold,
                                    unselectedColor = OutlineColor
                                )
                            )
                            Text(stringResource(id = R.string.color_white), color = WarmWhite)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedTurn = "b" }) {
                            RadioButton(
                                selected = selectedTurn == "b",
                                onClick = { selectedTurn = "b" },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = PrimaryGold,
                                    unselectedColor = OutlineColor
                                )
                            )
                            Text(stringResource(id = R.string.color_black), color = WarmWhite)
                        }
                    }
                }
            },
            containerColor = SurfaceGreen,
            confirmButton = {
                TextButton(
                    onClick = { 
                        if (viewModel.onSaveConfirmClicked(selectedTurn)) {
                            onSaveBoard(boardName)
                        }
                    },
                    enabled = boardName.isNotBlank()
                ) {
                    Text(
                        stringResource(id = R.string.editor_save_button),
                        color = if (boardName.isNotBlank()) PrimaryGold else WarmWhite.copy(alpha = 0.5f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissSaveDialog() }) {
                    Text(stringResource(id = R.string.button_cancel), color = WarmWhite)
                }
            }
        )
    }

    if (saveErrorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearSaveErrorMessage() },
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "Posición Inválida",
                    color = ErrorColor,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(stringResource(id = saveErrorMessage!!), color = WarmWhite)
            },
            containerColor = SurfaceGreen,
            confirmButton = {
                TextButton(onClick = { viewModel.clearSaveErrorMessage() }) {
                    Text("Entendido", color = PrimaryGold)
                }
            }
        )
    }

    // ─── Drop handler: board→board ────────────────────────────────────────────
    fun handleBoardDrop(source: DragSource, toRow: Int, toCol: Int) {
        when (source) {
            is DragSource.FromBoard -> {
                if (source.row == toRow && source.col == toCol) return
                viewModel.movePiece(source.row, source.col, toRow, toCol)
            }
            is DragSource.FromPalette -> {
                val piece = dndState.activeDrag?.piece ?: return
                viewModel.placePiece(toRow, toCol, piece)
            }
        }
    }

    // ─── Handle when a board piece is dropped outside the board ───────────────
    fun handleBoardDropOutside(source: DragSource.FromBoard, screenX: Float, screenY: Float) {
        val zone = deleteZoneBounds
        if (zone != null && screenX in zone.left..zone.right && screenY in zone.top..zone.bottom) {
            viewModel.removePiece(source.row, source.col)
        }
        // If dropped outside board but NOT on delete zone → piece stays (no action needed)
    }

    // ─── Handle palette drag end ───────────────────────────────────────────────
    fun handlePaletteDragEnd() {
        val drag = dndState.activeDrag
        try {
            if (drag == null) return
            val bounds = boardBoundsInWindow
            if (bounds == null) return

            val isOnBoard = drag.screenX in bounds.left..bounds.right &&
                            drag.screenY in bounds.top..bounds.bottom

            if (isOnBoard) {
                val cellW = bounds.width / 8f
                val cellH = bounds.height / 8f
                val row = ((drag.screenY - bounds.top) / cellH).toInt().coerceIn(0, 7)
                val col = ((drag.screenX - bounds.left) / cellW).toInt().coerceIn(0, 7)
                when (drag.source) {
                    is DragSource.FromPalette -> viewModel.placePiece(row, col, drag.piece)
                    is DragSource.FromBoard   -> viewModel.movePiece(drag.source.row, drag.source.col, row, col)
                }
            } else {
                // Palette pieces outside board are simply discarded
            }
        } finally {
            dndState.endDrag()
        }
    }

    // ─── UI ───────────────────────────────────────────────────────────────────–
    CompositionLocalProvider(LocalDragAndDropState provides dndState) {
        val radialGradient = Brush.radialGradient(
            colors = listOf(BackgroundGradientCenter, BackgroundGradientEdge),
            radius = Float.MAX_VALUE
        )

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = Color.Transparent,
            topBar = {
                SkaknnaTopAppBar(
                    title = stringResource(id = R.string.editor_title),
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(id = R.string.button_back),
                                tint = PrimaryGold
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { 
                            val parts = fen.split(" ")
                            selectedTurn = if (parts.size >= 2 && parts[1] == "b") "b" else "w"
                            viewModel.onSaveIconClicked()
                        }) {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = stringResource(id = R.string.editor_save_dialog_title),
                                tint = PrimaryGold,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier
                .fillMaxSize()
                .background(radialGradient)) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ── FEN input ──────────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceGreen)
                            .border(1.dp, OutlineColor, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        FenInput(
                            fen = fen,
                            onFenChange = { viewModel.updateFen(it) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // ── Validation warnings ────────────────────────────────────
                    if (!validation.isValid) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(ErrorColor.copy(alpha = 0.1f))
                                .border(2.dp, ErrorColor, RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.validation_invalid_position),
                                color = ErrorColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            validation.warnings.forEach { warning ->
                                val warningText = when(warning) {
                                    BoardWarning.WhiteNoKing -> stringResource(id = R.string.warning_white_no_king)
                                    BoardWarning.WhiteMultipleKings -> stringResource(id = R.string.warning_white_multiple_kings)
                                    BoardWarning.BlackNoKing -> stringResource(id = R.string.warning_black_no_king)
                                    BoardWarning.BlackMultipleKings -> stringResource(id = R.string.warning_black_multiple_kings)
                                    BoardWarning.WhitePawnRank8 -> stringResource(id = R.string.warning_white_pawn_rank8)
                                    BoardWarning.WhitePawnRank1 -> stringResource(id = R.string.warning_white_pawn_rank1)
                                    BoardWarning.BlackPawnRank8 -> stringResource(id = R.string.warning_black_pawn_rank8)
                                    BoardWarning.BlackPawnRank1 -> stringResource(id = R.string.warning_black_pawn_rank1)
                                    is BoardWarning.WhitePawnsCount -> stringResource(id = R.string.warning_white_pawns_count, warning.count)
                                    is BoardWarning.BlackPawnsCount -> stringResource(id = R.string.warning_black_pawns_count, warning.count)
                                }
                                Text(
                                    text = "• $warningText",
                                    color = WarmWhite,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // ── Action buttons ─────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Limpiar todo
                        ExtendedFloatingActionButton(
                            onClick = { viewModel.clearBoard() },
                            icon = { Icon(Icons.Default.Clear, contentDescription = null) },
                            text = { Text(stringResource(id = R.string.editor_clear_button), fontWeight = FontWeight.SemiBold) },
                            containerColor = SurfaceGreen,
                            contentColor = PrimaryGold,
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, PrimaryGold, RoundedCornerShape(16.dp))
                        )

                        // Posición inicial
                        ExtendedFloatingActionButton(
                            onClick = { viewModel.resetToStartPosition() },
                            icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                            text = { Text(stringResource(id = R.string.editor_reset_button), fontWeight = FontWeight.SemiBold) },
                            containerColor = SurfaceGreen,
                            contentColor = PrimaryGold,
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, PrimaryGold, RoundedCornerShape(16.dp))
                        )
                    }

                    // ── Board + palettes ───────────────────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceGreen)
                            .border(1.dp, OutlineColor, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Black palette
                        PiecePalette(
                            isBlack = true,
                            board = board,
                            modifier = Modifier.fillMaxWidth(),
                            onPieceDragEnd = { handlePaletteDragEnd() }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Interactive chess board
                        ChessBoard(
                            board = board,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            onBoardBoundsChanged = { boardBoundsInWindow = it },
                            onDrop = { source, toRow, toCol ->
                                handleBoardDrop(source, toRow, toCol)
                            },
                            onDroppedOutsideBoard = { source, screenX, screenY ->
                                handleBoardDropOutside(source, screenX, screenY)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // White palette
                        PiecePalette(
                            isBlack = false,
                            board = board,
                            modifier = Modifier.fillMaxWidth(),
                            onPieceDragEnd = { handlePaletteDragEnd() }
                        )
                    }
                } // Column

                // ── Delete zone (PiP-style) ────────────────────────────────────
                // Appears with a spring animation when a board piece is picked up.
                // Scales and glows red when the finger hovers over it.
                val deleteZoneScale by animateFloatAsState(
                    targetValue = if (isHoveringDelete) 1.35f else 1f,
                    animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
                    label = "deleteZoneScale"
                )
                val deleteZoneAlpha by animateFloatAsState(
                    targetValue = if (isHoveringDelete) 1f else 0.82f,
                    label = "deleteZoneAlpha"
                )

                AnimatedVisibility(
                    visible = isDraggingFromBoard,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 36.dp),
                    enter = fadeIn() + scaleIn(initialScale = 0.4f, animationSpec = spring(dampingRatio = 0.6f)),
                    exit  = fadeOut() + scaleOut(targetScale = 0.4f)
                ) {
                    Box(
                        modifier = Modifier
                            .onGloballyPositioned { coords ->
                                deleteZoneBounds = coords.boundsInWindow()
                            }
                            .graphicsLayer(
                                scaleX = deleteZoneScale,
                                scaleY = deleteZoneScale,
                                alpha  = deleteZoneAlpha
                            )
                            .size(64.dp)
                            .shadow(
                                elevation = if (isHoveringDelete) 20.dp else 8.dp,
                                shape = CircleShape,
                                ambientColor = Color.Red,
                                spotColor = Color.Red
                            )
                            .background(
                                color = if (isHoveringDelete) Color(0xFFCC1111) else Color(0xFF8B1A1A),
                                shape = CircleShape
                            )
                            .border(
                                width = if (isHoveringDelete) 3.dp else 1.5.dp,
                                color = if (isHoveringDelete) Color.White else Color.White.copy(alpha = 0.35f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(id = R.string.editor_delete_piece_button),
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // ── Floating drag piece overlay ────────────────────────────────
                if (activeDrag != null) {
                    val pieceRes = pieceDrawable(activeDrag.piece)
                    Image(
                        painter = painterResource(id = pieceRes),
                        contentDescription = null,
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (activeDrag.screenX - 36.dp.toPx()).roundToInt(),
                                    (activeDrag.screenY - 36.dp.toPx()).roundToInt()
                                )
                            }
                            .size(72.dp)
                            .shadow(12.dp, RoundedCornerShape(8.dp))
                            .graphicsLayer(
                                scaleX = 1.15f,
                                scaleY = 1.15f,
                                alpha = 0.92f
                            )
                    )
                }
            } // Box
        }
    }
}
