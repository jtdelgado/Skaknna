package com.skaknna.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FenInput(
    fen: String,
    onFenChange: (String) -> Unit,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = fen,
        onValueChange = onFenChange,
        isError = isError,
        label = { Text("Posición FEN") },
        modifier = modifier.fillMaxWidth(),
        singleLine = true
    )
}
