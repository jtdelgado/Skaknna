package com.skaknna.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skaknna.ui.theme.EvaluationWhite
import com.skaknna.ui.theme.EvaluationBlack
import com.skaknna.ui.theme.OutlineColor
import com.skaknna.ui.theme.SurfaceGreen

@Composable
fun EvaluationBar(
    evaluation: Float, 
    modifier: Modifier = Modifier
) {
    // Coerce evaluation and calculate the proportion of White (bottom part)
    val clampedEval = evaluation.coerceIn(-5f, 5f)
    val fillRatio = (clampedEval + 5f) / 10f

    // Prevent exactly 0 weight to avoid Compose layout crashes
    val whiteWeight = fillRatio.coerceIn(0.001f, 0.999f)
    val blackWeight = 1f - whiteWeight

    Column(
        modifier = modifier
            .width(6.dp)
            .fillMaxHeight()
    ) {
        // Black section (top)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(blackWeight)
                .background(EvaluationBlack)
        )
        
        // White section (bottom)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(whiteWeight)
                .background(EvaluationWhite)
        )
    }
}
