package com.skaknna.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BoardViewModel : ViewModel() {
    private val _fen = MutableStateFlow("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
    val fen: StateFlow<String> = _fen.asStateFlow()

    fun updateFen(newFen: String) {
        _fen.value = newFen
    }
}
