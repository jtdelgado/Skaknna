package com.skaknna.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skaknna.ui.components.*
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

    var showSaveDialog by remember { mutableStateOf(false) }
    var boardName by remember { mutableStateOf("") }
    // Eraser mode: tapping a piece on the board removes it
    var eraserMode by remember { mutableStateOf(false) }

    // ─── DnD State ────────────────────────────────────────────────────────────
    val dndState = remember { DragAndDropState() }

    // Track board bounds for palette→board drop detection
    var boardBoundsInWindow by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }

    // ─── Save dialog ──────────────────────────────────────────────────────────
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    "Guardar Tablero",
                    color = com.skaknna.ui.theme.GoldenYellow,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = boardName,
                    onValueChange = { boardName = it },
                    label = { Text("Nombre de la partida") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = com.skaknna.ui.theme.GoldenYellow,
                        unfocusedBorderColor = com.skaknna.ui.theme.WoodMedium,
                        focusedTextColor = com.skaknna.ui.theme.WarmWhite,
                        unfocusedTextColor = com.skaknna.ui.theme.WarmWhite,
                        cursorColor = com.skaknna.ui.theme.GoldenYellow,
                        focusedLabelColor = com.skaknna.ui.theme.GoldenYellow,
                        unfocusedLabelColor = com.skaknna.ui.theme.WarmWhite.copy(alpha = 0.7f)
                    )
                )
            },
            containerColor = com.skaknna.ui.theme.WoodDark,
            confirmButton = {
                TextButton(
                    onClick = { showSaveDialog = false; onSaveBoard(boardName) },
                    enabled = boardName.isNotBlank()
                ) {
                    Text(
                        "Guardar",
                        color = if (boardName.isNotBlank()) com.skaknna.ui.theme.GoldenYellow
                                else com.skaknna.ui.theme.WarmWhite.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancelar", color = com.skaknna.ui.theme.WarmWhite)
                }
            }
        )
    }

    // ─── Drop handler: board→board (called from ChessBoard's own onDragEnd) ───
    fun handleBoardDrop(source: DragSource, toRow: Int, toCol: Int) {
        when (source) {
            is DragSource.FromBoard -> {
                if (source.row == toRow && source.col == toCol) return
                viewModel.movePiece(source.row, source.col, toRow, toCol)
            }
            is DragSource.FromPalette -> {
                // Should not arrive here via board handler, but guard anyway
                val piece = dndState.activeDrag?.piece ?: return
                viewModel.placePiece(toRow, toCol, piece)
            }
        }
    }

    // ─── Handle every palette drag end (placement OR discard) ─────────────────
    // KEY FIX: The palette gesture owns the touch event from start to finish.
    // The board's pointerInput can never receive a palette-initiated gesture end.
    // So we do ALL drop detection here, after the palette's onDragEnd fires.
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
                // Dropped outside board: remove if it came from the board
                if (drag.source is DragSource.FromBoard) {
                    viewModel.removePiece(drag.source.row, drag.source.col)
                }
            }
        } finally {
            dndState.endDrag() // ALWAYS clear drag state — prevents stuck overlay
        }
    }

    // ─── UI ───────────────────────────────────────────────────────────────────
    CompositionLocalProvider(LocalDragAndDropState provides dndState) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Editor FEN",
                            color = com.skaknna.ui.theme.GoldenYellow,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Atrás",
                                tint = com.skaknna.ui.theme.GoldenYellow
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSaveDialog = true }) {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = "Guardar Tablero",
                                tint = com.skaknna.ui.theme.GoldenYellow,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = com.skaknna.ui.theme.TransparentColor)
                )
            }
        ) { paddingValues ->
            // Root Box so we can overlay the floating drag piece
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ── FEN input ─────────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(com.skaknna.ui.theme.WoodDark)
                            .border(2.dp, com.skaknna.ui.theme.WoodMedium, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        FenInput(
                            fen = fen,
                            onFenChange = { viewModel.updateFen(it) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // ── Validation warnings ───────────────────────────────────
                    if (!validation.isValid) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(com.skaknna.ui.theme.GoldGlow)
                                .border(2.dp, com.skaknna.ui.theme.GoldenYellow, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "⚠ Posición no válida",
                                color = com.skaknna.ui.theme.GoldenYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            validation.warnings.forEach { warning ->
                                Text(
                                    text = "• $warning",
                                    color = com.skaknna.ui.theme.WarmWhite,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    // ── Barra de acciones del tablero ──────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Borrar pieza (toggle)
                        val eraserContainerColor = if (eraserMode)
                            Color(0xFF8B1A1A)  // rojo madera cuando activo
                        else com.skaknna.ui.theme.WoodMedium
                        val eraserContentColor = if (eraserMode)
                            Color(0xFFFFAAAA)
                        else com.skaknna.ui.theme.GoldenYellow
                        val eraserBorderColor = if (eraserMode)
                            Color(0xFFCC3333)
                        else com.skaknna.ui.theme.WoodDark

                        ExtendedFloatingActionButton(
                            onClick = { eraserMode = !eraserMode },
                            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                            text = { Text(if (eraserMode) "Borrar: ON" else "Borrar", fontWeight = FontWeight.Bold) },
                            containerColor = eraserContainerColor,
                            contentColor = eraserContentColor,
                            modifier = Modifier
                                .weight(1f)
                                .border(3.dp, eraserBorderColor, RoundedCornerShape(16.dp))
                        )

                        // Limpiar todo
                        ExtendedFloatingActionButton(
                            onClick = { viewModel.clearBoard(); eraserMode = false },
                            icon = { Icon(Icons.Default.Clear, contentDescription = null) },
                            text = { Text("Limpiar", fontWeight = FontWeight.Bold) },
                            containerColor = com.skaknna.ui.theme.WoodMedium,
                            contentColor = com.skaknna.ui.theme.GoldenYellow,
                            modifier = Modifier
                                .weight(1f)
                                .border(3.dp, com.skaknna.ui.theme.WoodDark, RoundedCornerShape(16.dp))
                        )

                        // Posición inicial
                        ExtendedFloatingActionButton(
                            onClick = { viewModel.resetToStartPosition(); eraserMode = false },
                            icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                            text = { Text("Reiniciar", fontWeight = FontWeight.Bold) },
                            containerColor = com.skaknna.ui.theme.WoodMedium,
                            contentColor = com.skaknna.ui.theme.GoldenYellow,
                            modifier = Modifier
                                .weight(1f)
                                .border(3.dp, com.skaknna.ui.theme.WoodDark, RoundedCornerShape(16.dp))
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(com.skaknna.ui.theme.WoodDark)
                            .border(2.dp, com.skaknna.ui.theme.WoodMedium, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Black palette
                        PiecePalette(
                            isBlack = true,
                            board = board,
                            modifier = Modifier.fillMaxWidth(),
                            onPieceDragEnd = { handlePaletteDragEnd() }
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Interactive chess board
                        ChessBoard(
                            board = board,
                            eraserMode = eraserMode,
                            onCellTap = { row, col ->
                                if (eraserMode) {
                                    viewModel.removePiece(row, col)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coords ->
                                    boardBoundsInWindow = coords.boundsInWindow()
                                },
                            onDrop = { source, toRow, toCol ->
                                handleBoardDrop(source, toRow, toCol)
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // White palette
                        PiecePalette(
                            isBlack = false,
                            board = board,
                            modifier = Modifier.fillMaxWidth(),
                            onPieceDragEnd = { handlePaletteDragEnd() }
                        )
                    }

                }

                // ── Floating drag overlay ─────────────────────────────────────────
                val activeDrag = dndState.activeDrag
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
            }
        }
    }
}
