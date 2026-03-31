package com.skaknna.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.skaknna.ui.theme.WoodDark
import com.skaknna.ui.theme.WoodDarkCenter
import com.skaknna.ui.theme.WoodLight
import com.skaknna.ui.theme.WoodLightCenter

@Composable
fun ChessBoard(
    modifier: Modifier = Modifier,
    fen: String = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
) {
    val lightSquareBrush = Brush.radialGradient(
        colors = listOf(WoodLightCenter, WoodLight)
    )
    val darkSquareBrush = Brush.radialGradient(
        colors = listOf(WoodDarkCenter, WoodDark)
    )

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .background(WoodDark)
            .padding(6.dp)
    ) {
        for (row in 0..7) {
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                for (col in 0..7) {
                    val isLightSquare = (row + col) % 2 == 0
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(if (isLightSquare) lightSquareBrush else darkSquareBrush)
                            .border(0.5.dp, WoodDark.copy(alpha = 0.5f)) 
                    ) {
                        
                    }
                }
            }
        }
    }
}
