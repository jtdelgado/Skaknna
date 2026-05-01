package com.skaknna.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory para crear instancias de AuthViewModel con inyección de dependencias.
 * 
 * Proporciona el contexto y WEB_CLIENT_ID necesarios para la autenticación con Google.
 * 
 * @param context Contexto de la aplicación
 * @param webClientId Web Client ID obtenido de Google Cloud Console (via BuildConfig)
 */
class AuthViewModelFactory(
    private val context: Context,
    private val webClientId: String
) : ViewModelProvider.Factory {
    
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(context, webClientId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
