package com.skaknna.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.skaknna.ui.theme.EvaluationBlack
import com.skaknna.ui.theme.EvaluationWhite
import com.skaknna.ui.theme.OutlineColor

/**
 * Vertical Stockfish evaluation bar.
 *
 * Visual features:
 *  - 0.5dp olive-green border to anchor it against the board edge (no floating).
 *  - Sharp 1px divider line at the black/white boundary.
 *  - Small centered tick at the 0.0 (equality) midpoint.
 *  - Rounded corners (4dp) for a polished feel.
 */
@Composable
fun EvaluationBar(
    evaluation: Float,
    modifier: Modifier = Modifier
) {
    // Clamp to ±5 pawns and derive fill ratios
    val clampedEval = evaluation.coerceIn(-5f, 5f)
    // fillRatio = 1.0 → all white (huge white advantage); 0.0 → all black
    val fillRatio = (clampedEval + 5f) / 10f

    // Guard against zero-weight crash
    val whiteWeight = fillRatio.coerceIn(0.001f, 0.999f)
    val blackWeight = 1f - whiteWeight

    // Equality tick color: semi-transparent gold so it's subtle but visible
    val equalityTickColor = Color(0x99E6C85C) // PrimaryGold at ~60% opacity

    Box(
        modifier = modifier
            .width(8.dp)
            .fillMaxHeight()
            // Thin olive border gives the bar a frame, preventing "floating" look
            .border(0.5.dp, OutlineColor, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
    ) {
        // ── Two-section fill (black on top, white on bottom) ─────────────────
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(blackWeight)
                    .background(EvaluationBlack)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(whiteWeight)
                    .background(EvaluationWhite)
            )
        }

        // ── Overlay Canvas: divider line + equality tick ──────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val barH = size.height
            val barW = size.width

            // 1. Sharp divider line at the black/white boundary
            val dividerY = barH * blackWeight
            drawLine(
                color = Color.Gray.copy(alpha = 0.6f),
                start = Offset(0f, dividerY),
                end = Offset(barW, dividerY),
                strokeWidth = 1f
            )

            // 2. Small equality tick at the exact 0.0 midpoint (y = 50%)
            val tickY = barH * 0.5f
            val tickHalfWidth = barW * 0.45f
            drawLine(
                color = equalityTickColor,
                start = Offset(barW * 0.5f - tickHalfWidth, tickY),
                end = Offset(barW * 0.5f + tickHalfWidth, tickY),
                strokeWidth = 1.5f
            )
        }
    }
}
