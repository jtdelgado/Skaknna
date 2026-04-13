package com.skaknna.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skaknna.ui.components.ChessBoard
import com.skaknna.ui.components.EvaluationBar
import com.skaknna.viewmodel.BoardViewModel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    viewModel: BoardViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val board by viewModel.board.collectAsState()
    val bestMove by viewModel.bestMove.collectAsState()
    val evaluation by viewModel.evaluation.collectAsState()
    val analysisLine by viewModel.analysisLine.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()

    // Manage screen lifecycle to suppress FEN listener analysis during board selection
    DisposableEffect(Unit) {
        viewModel.enterAnalysisScreen()
        onDispose {
            viewModel.exitAnalysisScreen()
        }
    }

    // Trigger analysis when entering the screen
    LaunchedEffect(Unit) {
        viewModel.analyzeCurrentPosition()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Análisis Táctico",
                        color = com.skaknna.ui.theme.GoldenYellow,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = com.skaknna.ui.theme.GoldenYellow
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = com.skaknna.ui.theme.TransparentColor),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ajustes",
                            tint = com.skaknna.ui.theme.GoldenYellow
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EvaluationBar(evaluation = evaluation, modifier = Modifier.height(300.dp))
                // ChessBoard now takes board matrix; read-only (no onDrop callback)
                ChessBoard(board = board, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Analysis Title with Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Mejores Movimientos (Stockfish 18)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = com.skaknna.ui.theme.GoldenYellow
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Analysis Results Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (bestMove.isNotEmpty()) {
                        // Best Move Section
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Mejor Movimiento",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    bestMove,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = com.skaknna.ui.theme.GoldenYellow
                                )
                            }
                            
                            // Evaluation Badge
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = when {
                                            evaluation > 0.5f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            evaluation < -0.5f -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    formatEvaluation(evaluation),
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        evaluation > 0.5f -> MaterialTheme.colorScheme.primary
                                        evaluation < -0.5f -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }

                        if (analysisLine.isNotEmpty()) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            
                            Text(
                                "Variante Principal",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                analysisLine,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else if (isAnalyzing) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = com.skaknna.ui.theme.GoldenYellow
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Analizando posición...")
                        }
                    } else {
                        Text(
                            "Análisis no disponible",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Formats the evaluation score for display.
 * Positive values show as "+X.X" (white advantage)
 * Negative values show as "X.X" (black advantage)
 * Special values for checkmate (e.g., "#5")
 */
private fun formatEvaluation(eval: Float): String {
    return when {
        eval >= 10f -> "#${(eval - 10f).toInt()}" // White mate
        eval <= -10f -> "#${(eval + 10f).toInt()}" // Black mate
        eval > 0 -> String.format("+%.1f", eval)
        else -> String.format("%.1f", eval)
    }
}
