package com.skaknna.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skaknna.ui.components.ChessBoard
import com.skaknna.ui.components.FenInput
import com.skaknna.ui.components.PiecePalette
import com.skaknna.viewmodel.BoardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: BoardViewModel,
    onSaveBoard: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val fen by viewModel.fen.collectAsState()
    var showSaveDialog by remember { mutableStateOf(false) }
    var boardName by remember { mutableStateOf("") }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Guardar Tablero", color = com.skaknna.ui.theme.GoldenYellow, fontWeight = FontWeight.Bold) },
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
                        unfocusedLabelColor = com.skaknna.ui.theme.WarmWhite.copy(alpha=0.7f)
                    )
                )
            },
            containerColor = com.skaknna.ui.theme.WoodDark,
            confirmButton = {
                TextButton(
                    onClick = {
                        showSaveDialog = false
                        onSaveBoard(boardName)
                    },
                    enabled = boardName.isNotBlank()
                ) {
                    Text("Guardar", color = if (boardName.isNotBlank()) com.skaknna.ui.theme.GoldenYellow else com.skaknna.ui.theme.WarmWhite.copy(alpha=0.5f), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancelar", color = com.skaknna.ui.theme.WarmWhite)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editor FEN", color = com.skaknna.ui.theme.GoldenYellow, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = com.skaknna.ui.theme.GoldenYellow)
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Panel Sólido Independiente para Input FEN
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

            // Panel Unificado que engloba Fichas Negras + Tablero + Fichas Blancas
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(com.skaknna.ui.theme.WoodDark)
                    .border(2.dp, com.skaknna.ui.theme.WoodMedium, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PiecePalette(isBlack = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                ChessBoard(fen = fen, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                PiecePalette(isBlack = false, modifier = Modifier.fillMaxWidth())
            }

            // Botón de Guardar separado de los bloques principales
            ExtendedFloatingActionButton(
                onClick = { showSaveDialog = true },
                icon = { Icon(Icons.Default.Save, contentDescription = "Guardar Tablero") },
                text = { Text("Guardar Tablero", fontWeight = FontWeight.Bold) },
                containerColor = com.skaknna.ui.theme.WoodMedium,
                contentColor = com.skaknna.ui.theme.GoldenYellow,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(3.dp, com.skaknna.ui.theme.WoodDark, RoundedCornerShape(16.dp))
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
