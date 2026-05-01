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
import androidx.compose.ui.res.stringResource
import com.skaknna.R
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
                title = { Text(stringResource(id = R.string.settings_title), fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.button_back))
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
                text = stringResource(id = R.string.settings_chess_analysis_section),
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
                    text = stringResource(id = R.string.settings_analysis_depth_label),
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
                        analysisDepth.value <= 5 -> stringResource(id = R.string.settings_depth_fast_icon)
                        analysisDepth.value <= 12 -> stringResource(id = R.string.settings_depth_balanced_icon)
                        else -> stringResource(id = R.string.settings_depth_deep_icon)
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
            stringResource(id = R.string.settings_depth_instant)
        }
        depth <= 18 -> {
            stringResource(id = R.string.settings_depth_fast)
        }
        depth <= 24 -> {
            stringResource(id = R.string.settings_depth_advanced)
        }
        depth <= 30 -> {
            stringResource(id = R.string.settings_depth_deep)
        }
        else -> {
            stringResource(id = R.string.settings_depth_max)
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
