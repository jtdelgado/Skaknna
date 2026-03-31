package com.skaknna.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToScanner: () -> Unit,
    onNavigateToEditor: () -> Unit,
    onNavigateToAnalysis: () -> Unit
) {
    // Estado Mock para simular sesión
    var isLoggedIn by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tableros Guardados", color = com.skaknna.ui.theme.GoldenYellow, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = com.skaknna.ui.theme.TransparentColor),
                actions = {
                    IconButton(
                        onClick = { isLoggedIn = !isLoggedIn },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (isLoggedIn) {
                            // Placeholder de foto de Google
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(com.skaknna.ui.theme.LeafGreen)
                                    .border(2.dp, com.skaknna.ui.theme.WoodDark, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("G", color = com.skaknna.ui.theme.WarmWhite, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            // Usuario desconocido
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Iniciar Sesión",
                                tint = com.skaknna.ui.theme.WarmWhite,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            Column {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToScanner,
                    icon = { Icon(Icons.Default.CameraAlt, contentDescription = "Cámara") },
                    text = { Text("Escanear con Cámara", fontWeight = FontWeight.Bold) },
                    containerColor = com.skaknna.ui.theme.WoodMedium,
                    contentColor = com.skaknna.ui.theme.GoldenYellow,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .border(3.dp, com.skaknna.ui.theme.WoodDark, RoundedCornerShape(16.dp))
                )
                ExtendedFloatingActionButton(
                    onClick = onNavigateToEditor,
                    icon = { Icon(Icons.Default.Add, contentDescription = "Nuevo") },
                    text = { Text("Nuevo Tablero Manual", fontWeight = FontWeight.Bold) },
                    containerColor = com.skaknna.ui.theme.WoodMedium,
                    contentColor = com.skaknna.ui.theme.GoldenYellow,
                    modifier = Modifier
                        .border(3.dp, com.skaknna.ui.theme.WoodDark, RoundedCornerShape(16.dp))
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(5) { index ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .border(3.dp, com.skaknna.ui.theme.WoodDark, RoundedCornerShape(12.dp))
                        .clickable { onNavigateToAnalysis() },
                    colors = CardDefaults.cardColors(
                        containerColor = com.skaknna.ui.theme.WoodMedium,
                        contentColor = com.skaknna.ui.theme.WarmWhite
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Partida Guardada #${index + 1}", 
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = com.skaknna.ui.theme.GoldenYellow
                        )
                        Text(
                            text = "Fecha: 10/10/2023", 
                            style = MaterialTheme.typography.bodyMedium,
                            color = com.skaknna.ui.theme.WarmWhite
                        )
                    }
                }
            }
        }
    }
}
