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

// DataStore extension
private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsViewModel(private val context: Context) : ViewModel() {

    companion object {
        private val ANALYSIS_DEPTH_KEY = intPreferencesKey("analysis_depth")
        private const val DEFAULT_ANALYSIS_DEPTH = 5
        private const val MIN_DEPTH = 1
        private const val MAX_DEPTH = 20
    }

    val analysisDepth: StateFlow<Int> = context.dataStore.data
        .map { preferences -> preferences[ANALYSIS_DEPTH_KEY] ?: DEFAULT_ANALYSIS_DEPTH }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DEFAULT_ANALYSIS_DEPTH
        )

    /**
     * Updates the analysis depth setting and persists to DataStore.
     * 
     * @param depth The new depth value (1-20)
     */
    fun setAnalysisDepth(depth: Int) {
        val clampedDepth = depth.coerceIn(MIN_DEPTH, MAX_DEPTH)
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[ANALYSIS_DEPTH_KEY] = clampedDepth
            }
        }
    }

    /**
     * Gets the estimated analysis time based on depth level.
     * These are rough estimates for typical positions.
     */
    fun getEstimatedAnalysisTime(depth: Int): String {
        return when {
            depth <= 3 -> "< 1 second"
            depth <= 6 -> "1-2 seconds"
            depth <= 10 -> "2-5 seconds"
            depth <= 15 -> "5-10 seconds"
            else -> "> 10 seconds"
        }
    }
}

class SettingsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(context) as T
    }
}
