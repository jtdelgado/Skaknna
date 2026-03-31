package com.skaknna.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.skaknna.ui.theme.LeafGreen
import com.skaknna.ui.theme.WoodDark

@Composable
fun EvaluationBar(
    evaluation: Float, 
    modifier: Modifier = Modifier
) {
    val clampedEval = evaluation.coerceIn(-5f, 5f)
    val fillRatio = (clampedEval + 5f) / 10f

    val infiniteTransition = rememberInfiniteTransition(label = "EvalPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AlphaPulse"
    )

    val leafGradient = Brush.verticalGradient(
        colors = listOf(LeafGreen.copy(alpha = pulseAlpha), LeafGreen.copy(alpha = pulseAlpha * 0.3f))
    )

    Box(
        modifier = modifier
            .width(24.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(6.dp))
            .background(WoodDark)
            .border(1.dp, LeafGreen.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(fillRatio)
                .background(leafGradient)
        )
    }
}
