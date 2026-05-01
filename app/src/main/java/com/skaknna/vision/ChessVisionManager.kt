package com.skaknna.vision

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChessVisionManager(private val apiKey: String) {
    
    private val systemPrompt = """
        Actúa como un Gran Maestro de Ajedrez y experto en visión artificial.
        Analiza la imagen siguiendo estos pasos internos para asegurar precisión:
        1. Identifica la orientación del tablero (¿qué color está abajo?).
        2. Recorre el tablero fila por fila, desde la octava fila (superior) hasta la primera (inferior).
        3. Para cada fila, identifica las piezas de la columna 'a' a la 'h'.
        4. Convierte ese análisis al formato FEN.

        REGLAS:
        - Solo devuelve la cadena FEN.
        - No incluyas explicaciones.
        - Si el tablero está vacío o no es claro, devuelve 8/8/8/8/8/8/8/8.
    """.trimIndent()

    // FIX #3: Lista de modelos con IDs reales y verificados en la API de Gemini (Abril 2026).
    // "gemini-flash-latest" y "gemini-3.1-pro-preview" no existen → causaban 404.
    private val modelNames = listOf(
        "gemini-2.5-flash-preview-04-17", // Flash 2.5 estable: rápido y con razonamiento
        "gemini-2.0-flash",               // Flash 2.0: fallback rápido y muy disponible
        "gemini-2.5-pro-preview-05-06",   // Pro 2.5: máxima precisión si Flash falla
        "gemini-1.5-pro"                  // Último recurso: modelo estable y probado
    )

    suspend fun analyzeBoard(boardBitmap: Bitmap): VisionState = withContext(Dispatchers.IO) {
        var lastException: Exception? = null

        for (modelName in modelNames) {
            try {
                val model = GenerativeModel(
                    modelName = modelName,
                    apiKey = apiKey
                    // QUITAMOS systemInstruction para evitar problemas de retrocompatibilidad
                )

                val response = model.generateContent(
                    content {
                        image(boardBitmap) // Se envía la imagen original sin comprimir
                        text(systemPrompt) // La instrucción va como texto normal junto a la imagen
                    }
                )

                val fenResult = response.text?.trim()
                
                if (!fenResult.isNullOrEmpty() && fenResult.contains("/")) {
                    return@withContext VisionState.Success(fenResult)
                } else {
                    // La API contestó con éxito (por eso te cobra cuota), pero el texto 
                    // no contiene formato FEN o está vacío. 
                    // Esto suele pasar si un filtro de seguridad bloquea la respuesta o si no entendió la foto.
                    // Retornamos de inmediato para ver lo que dijo realmente la IA.
                    val blockReason = response.promptFeedback?.blockReason?.name
                    if (blockReason != null) {
                        return@withContext VisionState.Error("Bloqueado por filtro de seguridad de Google: $blockReason")
                    } else if (fenResult.isNullOrEmpty()) {
                        return@withContext VisionState.Error("La IA analizó la foto pero devolvió una respuesta vacía.")
                    } else {
                        return@withContext VisionState.Error("La IA contestó esto en lugar del FEN: ${fenResult.take(100)}")
                    }
                }
            } catch (e: Exception) {
                lastException = e
                val errorMessage = e.message ?: ""
                android.util.Log.e("ChessVision", "Modelo $modelName falló con: $errorMessage")
                // El SDK a veces lanza "Something unexpected happened" o errores raros.
                // Lo más seguro es intentar con el siguiente modelo pase lo que pase, a menos
                // que sea literal un error de conectividad global o de Auth, pero por si acaso, probamos los 4.
                continue 
            }
        }

        // Si llegamos aquí, fallaron todos
        VisionState.Error("Error: ${lastException?.localizedMessage ?: "No se pudo obtener FEN"}")
    }
}
