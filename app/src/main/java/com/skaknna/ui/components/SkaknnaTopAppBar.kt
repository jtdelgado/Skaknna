package com.skaknna.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skaknna.ui.theme.BackgroundGradientCenter
import com.skaknna.ui.theme.PrimaryGold
import com.skaknna.ui.theme.SurfaceGreen

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior

/**
 * SkaknnaTopAppBar provides a custom LargeTopAppBar with an adaptive title
 * that scales gracefully on narrow screens.
 *
 * Features:
 * - Always-dark container: colors are pinned to app tokens, ignoring system light/dark mode.
 * - Transparent background when expanded (gradient shows through).
 * - BackgroundGradientCenter when collapsed — never turns white in light mode.
 * - Auto-scaling title to avoid truncation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkaknnaTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val collapsedFraction = scrollBehavior?.state?.collapsedFraction ?: 0f

    LargeTopAppBar(
        title = {
            // Identify which title slot is rendering based on default text styles
            val isExpandedSlot = androidx.compose.material3.LocalTextStyle.current.fontSize > 22.sp
            
            // Clean transition: Expanded disappears fast, collapsed appears at the very end
            val alpha = if (isExpandedSlot) {
                1f - (collapsedFraction / 0.5f).coerceIn(0f, 1f)
            } else {
                ((collapsedFraction - 0.7f) / 0.3f).coerceIn(0f, 1f)
            }

            Text(
                text = title,
                color = PrimaryGold.copy(alpha = alpha),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = modifier,
        navigationIcon = navigationIcon ?: {},
        actions = actions,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            // Static colors for both states ensure a seamless, solid surface with no flicker/shadow
            containerColor = BackgroundGradientCenter,
            scrolledContainerColor = BackgroundGradientCenter,
            titleContentColor = PrimaryGold,
            actionIconContentColor = PrimaryGold,
            navigationIconContentColor = PrimaryGold,
        ),
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    minFontSize: Float = 12f
) {
    var textStyle by remember(text) { mutableStateOf(style) }
    var readyToDraw by remember(text) { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        color = color,
        style = textStyle,
        maxLines = 1,
        softWrap = false,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow && textStyle.fontSize.value > minFontSize) {
                textStyle = textStyle.copy(fontSize = (textStyle.fontSize.value * 0.9f).sp)
            } else {
                readyToDraw = true
            }
        }
    )
}
