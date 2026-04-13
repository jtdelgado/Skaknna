package com.skaknna.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skaknna.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val analysisDepth = viewModel.analysisDepth.collectAsState()
    val estimatedTime = viewModel.getEstimatedAnalysisTime(analysisDepth.value)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Analysis Depth Section
            Text(
                text = "Análisis de Ajedrez",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Depth Label and Value
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profundidad de análisis:",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = analysisDepth.value.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Depth Slider
            Slider(
                value = analysisDepth.value.toFloat(),
                onValueChange = { newValue ->
                    viewModel.setAnalysisDepth(newValue.toInt())
                },
                valueRange = 1f..20f,
                steps = 18, // Steps for 1-20 with step size of 1
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            // Depth Description
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        analysisDepth.value <= 5 -> "🔋 Rápido"
                        analysisDepth.value <= 12 -> "⚖️ Balanceado"
                        else -> "🧠 Profundo"
                    },
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = estimatedTime,
                    fontSize = 12.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Information Card
            DepthExplanationCard(analysisDepth.value)
        }
    }
}

@Composable
private fun DepthExplanationCard(depth: Int) {
    val explanation = when {
        depth <= 12 -> {
            "Análisis instantáneo. Ideal para detectar errores graves de inmediato."
        }
        depth <= 18 -> {
            "Análisis rápido. Nivel de Gran Maestro, perfecto para juego fluido."
        }
        depth <= 24 -> {
            "Análisis avanzado. Evalúa variantes estratégicas con gran precisión."
        }
        depth <= 30 -> {
            "Análisis profundo. Detecta sutilezas que solo los mejores motores ven."
        }
        else -> {
            "Análisis de máxima precisión. Fuerza bruta total para posiciones críticas."
        }
    }

    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = explanation,
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
