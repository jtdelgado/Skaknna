package com.skaknna.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.skaknna.R
import com.skaknna.ui.components.AnalysisCard
import com.skaknna.ui.components.SkaknnaTopAppBar
import com.skaknna.ui.theme.*
import com.skaknna.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val analysisDepth = viewModel.analysisDepth.collectAsState()
    val estimatedTime = viewModel.getEstimatedAnalysisTime(analysisDepth.value)

    val radialGradient = Brush.radialGradient(
        colors = listOf(BackgroundGradientCenter, BackgroundGradientEdge),
        radius = Float.MAX_VALUE
    )
    
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        topBar = {
            SkaknnaTopAppBar(
                title = stringResource(id = R.string.settings_title),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.button_back), tint = PrimaryGold)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
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
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(16.dp))

            // Analysis Depth Section
            Text(
                text = stringResource(id = R.string.settings_chess_analysis_section),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = WarmWhite
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
                    fontSize = 14.sp,
                    color = WarmWhite
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = analysisDepth.value.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGold
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
                steps = 18,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                colors = SliderDefaults.colors(
                    thumbColor = PrimaryGold,
                    activeTrackColor = LeafGreen,
                    inactiveTrackColor = OutlineColor
                )
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
                    fontSize = 12.sp,
                    color = WarmWhite
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = estimatedTime,
                    fontSize = 12.sp,
                    color = DeepEspresso
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Information Card
            DepthExplanationCard(analysisDepth.value)
                }
            }
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

    AnalysisCard(
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
            color = WarmWhite
        )
    }
}
