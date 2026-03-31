package com.skaknna.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onValidationComplete: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var isProcessing by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val laserAnimationOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 240f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "laser_y"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escanear Tablero", color = com.skaknna.ui.theme.GoldenYellow, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, style = MaterialTheme.typography.headlineMedium) },
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
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isProcessing) {
                CircularProgressIndicator(color = com.skaknna.ui.theme.GoldenYellow)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Analizando imagen con IA...", color = com.skaknna.ui.theme.WarmWhite)
                
                LaunchedEffect(Unit) {
                    delay(2500)
                    onValidationComplete()
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(250.dp)
                            .border(1.dp, com.skaknna.ui.theme.GoldenYellow.copy(alpha = 0.5f))
                            .background(com.skaknna.ui.theme.WoodGlassBackground)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .offset(y = laserAnimationOffset.dp)
                                .background(com.skaknna.ui.theme.GoldenYellow)
                        )
                        Text("Encuadre del tablero", color = com.skaknna.ui.theme.WarmWhite, modifier = Modifier.align(Alignment.Center))
                    }
                }

                ExtendedFloatingActionButton(
                    onClick = { isProcessing = true },
                    icon = { Icon(Icons.Default.Camera, contentDescription = "Capturar") },
                    text = { Text("Capturar y Traducir", fontWeight = FontWeight.Bold) },
                    containerColor = com.skaknna.ui.theme.WoodMedium,
                    contentColor = com.skaknna.ui.theme.GoldenYellow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .border(3.dp, com.skaknna.ui.theme.WoodDark, RoundedCornerShape(16.dp))
                )
            }
        }
    }
}
