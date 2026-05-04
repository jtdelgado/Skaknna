package com.skaknna.viewmodel

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.skaknna.R

enum class AnalysisLevel(
    val depth: Int,
    val titleRes: Int,
    val descRes: Int,
    val timeRes: Int
) {
    QUICK(14, R.string.analysis_level_quick, R.string.analysis_desc_quick, R.string.analysis_time_quick),
    STANDARD(18, R.string.analysis_level_standard, R.string.analysis_desc_standard, R.string.analysis_time_standard),
    DEEP(22, R.string.analysis_level_deep, R.string.analysis_desc_deep, R.string.analysis_time_deep),
    ULTRA(26, R.string.analysis_level_ultra, R.string.analysis_desc_ultra, R.string.analysis_time_ultra)
}

// DataStore extension
private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsViewModel(private val context: Context) : ViewModel() {

    companion object {
        private val ANALYSIS_LEVEL_KEY = intPreferencesKey("analysis_level")
        private val DEFAULT_ANALYSIS_LEVEL = AnalysisLevel.STANDARD
    }

    val analysisLevel: StateFlow<AnalysisLevel> = context.dataStore.data
        .map { preferences -> 
            val levelIndex = preferences[ANALYSIS_LEVEL_KEY] ?: DEFAULT_ANALYSIS_LEVEL.ordinal
            AnalysisLevel.values().getOrNull(levelIndex) ?: DEFAULT_ANALYSIS_LEVEL
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DEFAULT_ANALYSIS_LEVEL
        )

    fun setAnalysisLevel(levelOrdinal: Int) {
        val clampedLevel = levelOrdinal.coerceIn(0, AnalysisLevel.values().size - 1)
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[ANALYSIS_LEVEL_KEY] = clampedLevel
            }
        }
    }
}

class SettingsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(context) as T
    }
}
