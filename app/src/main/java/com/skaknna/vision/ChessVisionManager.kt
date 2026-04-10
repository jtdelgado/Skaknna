package com.skaknna.vision

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChessVisionManager(apiKey: String) {
    private val systemPrompt = """
        Analiza esta imagen de un tablero de ajedrez físico. Identifica la posición exacta de cada pieza y genera exclusivamente la notación FEN (Forsyth-Edwards Notation) resultante. No añadas texto explicativo, solo la cadena FEN.
    """.trimIndent()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey,
        systemInstruction = content { text(systemPrompt) }
    )

    suspend fun analyzeBoard(boardBitmap: Bitmap): VisionState = withContext(Dispatchers.IO) {
        try {
            val response = generativeModel.generateContent(
                content {
                    image(boardBitmap)
                }
            )

            val fenResult = response.text?.trim()
            
            if (fenResult.isNullOrEmpty()) {
                VisionState.Error("La IA no devolvió ningún resultado.")
            } else {
                // Validación básica de FEN (esperamos que contenga al menos algunas barras)
                if (fenResult.contains("/")) {
                    VisionState.Success(fenResult)
                } else {
                    VisionState.Error("El resultado devuelto no parece una notación FEN válida: ${fenResult.take(20)}...")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            VisionState.Error("Error de la API: ${e.localizedMessage ?: "Desconocido"}")
        }
    }
}
