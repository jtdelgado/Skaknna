package com.skaknna.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skaknna.ui.components.ChessBoard
import com.skaknna.ui.components.EvaluationBar
import com.skaknna.viewmodel.BoardViewModel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    viewModel: BoardViewModel,
    onNavigateBack: () -> Unit
) {
    val board by viewModel.board.collectAsState()

    // Mock Stockfish evaluation
    val evaluation = remember { mutableStateOf(1.5f) }

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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = com.skaknna.ui.theme.TransparentColor)
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EvaluationBar(evaluation = evaluation.value, modifier = Modifier.height(300.dp))
                // ChessBoard now takes board matrix; read-only (no onDrop callback)
                ChessBoard(board = board, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Variantes principales (Stockfish 16 Mock)",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("1. e4 e5 2. Nf3 Nc6 (+1.5)")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("2. d4 d5 3. c4 dxc4 (+1.1)")
                }
            }
        }
    }
}
