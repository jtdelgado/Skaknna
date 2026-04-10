package com.skaknna.vision

sealed interface VisionState {
    data object Idle : VisionState
    data object Analyzing : VisionState
    data class Success(val fen: String) : VisionState
    data class Error(val message: String) : VisionState
}
