package com.skaknna.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skaknna.ui.theme.BlackColor
import com.skaknna.ui.theme.WarmWhite
import com.skaknna.ui.theme.WoodLight
import com.skaknna.ui.theme.WoodMedium

@Composable
fun PiecePalette(isBlack: Boolean, modifier: Modifier = Modifier) {
    val pieces = if (isBlack) {
        listOf("♚", "♛", "♜", "♝", "♞", "♟")
    } else {
        listOf("♔", "♕", "♖", "♗", "♘", "♙")
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        pieces.forEach { piece ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(WoodMedium)
                    .border(2.dp, WoodLight.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = piece,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isBlack) BlackColor else WarmWhite
                )
            }
        }
    }
}
