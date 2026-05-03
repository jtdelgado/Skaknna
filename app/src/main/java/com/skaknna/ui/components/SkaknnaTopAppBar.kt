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
 * - Transparent background handling for edge-to-edge aesthetics.
 * - Auto-scaling text to avoid truncation.
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
    val maxFontSize = 32f
    val minFontSize = 20f
    val currentFontSize = (maxFontSize - (maxFontSize - minFontSize) * collapsedFraction).sp

    LargeTopAppBar(
        title = {
            Text(
                text = title,
                color = PrimaryGold,
                fontSize = currentFontSize,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = modifier,
        navigationIcon = navigationIcon ?: {},
        actions = actions,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = MaterialTheme.colorScheme.background // Matches app background
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
