package com.skaknna.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.skaknna.ui.theme.BoardBrown
import com.skaknna.ui.theme.BoardCream

fun Modifier.checkerboardBackground(): Modifier = this.drawBehind {
    val squareSize = 64.dp.toPx()
    val columns = (size.width / squareSize).toInt() + 1
    val rows = (size.height / squareSize).toInt() + 1

    for (row in 0 until rows) {
        for (col in 0 until columns) {
            val isLight = (row + col) % 2 == 0
            val squareColor = if (isLight) BoardCream else BoardBrown
            
            drawRect(
                color = squareColor,
                topLeft = Offset(col * squareSize, row * squareSize),
                size = Size(squareSize + 1f, squareSize + 1f) // +1f padding to avoid sub-pixel rendering seams
            )
        }
    }
}
