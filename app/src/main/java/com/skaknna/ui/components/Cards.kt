package com.skaknna.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.skaknna.ui.theme.DeepEspresso
import com.skaknna.ui.theme.GoldGlow
import com.skaknna.ui.theme.GreenGlow
import com.skaknna.ui.theme.LeafGreen
import com.skaknna.ui.theme.OutlineColor
import com.skaknna.ui.theme.SurfaceGreen

// ============================================================================
// SURFACE CARD - Generic container for content
// ============================================================================
/**
 * [SurfaceCard] is a reusable container with Modern Classic styling:
 * - Background: SurfaceGreen
 * - Border: 1.dp Outline (no drop shadows)
 * - Corner Radius: 16.dp
 * - Padding: 16.dp (can be customized)
 *
 * Use for general containers, lists items, and sections.
 */
@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    padding: Int = 16,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceGreen),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = OutlineColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(padding.dp),
            content = content
        )
    }
}

// ============================================================================
// ACTION CARD - Interactive container with hover state
// ============================================================================
/**
 * [ActionCard] is an interactive [SurfaceCard] with:
 * - Ripple effect on tap
 * - Subtle gold overlay on hover (5% opacity)
 * - Same styling as [SurfaceCard]
 *
 * Use for clickable items like board cards in library, action buttons, etc.
 */
@Composable
fun ActionCard(
    modifier: Modifier = Modifier,
    padding: Int = 16,
    onClick: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = GoldGlow),
                onClick = onClick
            ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceGreen),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = OutlineColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(padding.dp),
            content = content
        )
    }
}

// ============================================================================
// ANALYSIS CARD - Emphasized container for metrics and statistics
// ============================================================================
/**
 * [AnalysisCard] is a specialized card for displaying analysis metrics:
 * - Background: SurfaceGreen
 * - Border: 2.dp LeafGreen (emphasis on analysis content)
 * - Corner Radius: 16.dp
 * - Padding: 24.dp (increased breathing room)
 *
 * Use for Stockfish depth display, evaluation metrics, move statistics, etc.
 */
@Composable
fun AnalysisCard(
    modifier: Modifier = Modifier,
    padding: Int = 24,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceGreen),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = LeafGreen
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(padding.dp),
            content = content
        )
    }
}

// ============================================================================
// ERROR CARD - Card for error messages and warnings
// ============================================================================
/**
 * [ErrorCard] is a card for displaying error or warning messages:
 * - Background: SurfaceGreen with slight red tint awareness
 * - Border: 2.dp Error color
 * - Corner Radius: 16.dp
 * - Padding: 16.dp
 *
 * Use for validation errors, alerts, and destructive action confirmations.
 */
@Composable
fun ErrorCard(
    modifier: Modifier = Modifier,
    padding: Int = 16,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceGreen),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = MaterialTheme.colorScheme.error
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(padding.dp),
            content = content
        )
    }
}
