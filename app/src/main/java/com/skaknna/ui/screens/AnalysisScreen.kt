package com.skaknna.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.skaknna.R
import com.skaknna.ui.components.AnalysisCard
import com.skaknna.ui.components.ChessBoard
import com.skaknna.ui.components.EvaluationBar
import com.skaknna.ui.components.SkaknnaTopAppBar
import com.skaknna.ui.theme.*
import com.skaknna.viewmodel.BoardViewModel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Refresh

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
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

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
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        topBar = {
            SkaknnaTopAppBar(
                title = stringResource(id = R.string.screen_title_analysis),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.button_back),
                            tint = PrimaryGold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.analyzeCurrentPosition() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(id = R.string.button_refresh),
                            tint = PrimaryGold
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(id = R.string.button_settings),
                            tint = PrimaryGold
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        val radialGradient = Brush.radialGradient(
            colors = listOf(BackgroundGradientCenter, BackgroundGradientEdge),
            radius = Float.MAX_VALUE
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(radialGradient),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 16.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            )
        ) {
            item {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                // ChessBoard width = total width - EvaluationBar width (6.dp) - spacing (8.dp)
                val boardSize = maxWidth - 14.dp

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(boardSize),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EvaluationBar(evaluation = evaluation, modifier = Modifier.fillMaxHeight())
                    // ChessBoard takes the exact calculated size to maintain 1:1 aspect ratio
                    ChessBoard(board = board, modifier = Modifier.size(boardSize))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Analysis Title with Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    stringResource(id = R.string.analysis_best_moves_section),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = WarmWhite
                )
                
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = PrimaryGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Analysis Results Card
            AnalysisCard(modifier = Modifier.fillMaxWidth().wrapContentHeight(), padding = 24) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                                stringResource(id = R.string.analysis_best_move_label),
                                fontSize = 12.sp,
                                color = DeepEspresso,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                bestMove,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGold
                            )
                        }
                        
                        // Evaluation Badge
                        Box(
                            modifier = Modifier
                                .background(
                                    color = when {
                                        evaluation > 0.5f -> LeafGreen.copy(alpha = 0.2f)
                                        evaluation < -0.5f -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                        else -> OutlineColor
                                    },
                                    shape = MaterialTheme.shapes.small
                                )
                                .border(
                                    width = 1.dp,
                                    color = when {
                                        evaluation > 0.5f -> LeafGreen
                                        evaluation < -0.5f -> MaterialTheme.colorScheme.error
                                        else -> OutlineColor
                                    },
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                formatEvaluation(evaluation),
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    evaluation > 0.5f -> LeafGreen
                                    evaluation < -0.5f -> MaterialTheme.colorScheme.error
                                    else -> WarmWhite
                                }
                            )
                        }
                    }

                    if (analysisLine.isNotEmpty()) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = OutlineColor)
                        
                        Text(
                            stringResource(id = R.string.analysis_principal_variation),
                            fontSize = 12.sp,
                            color = DeepEspresso,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            analysisLine,
                            style = MaterialTheme.typography.bodySmall,
                            color = WarmWhite
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
                            color = PrimaryGold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(id = R.string.analysis_analyzing), color = WarmWhite)
                    }
                    } else {
                        Text(
                            stringResource(id = R.string.analysis_not_available),
                            style = MaterialTheme.typography.bodySmall,
                            color = DeepEspresso
                        )
                    }
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
