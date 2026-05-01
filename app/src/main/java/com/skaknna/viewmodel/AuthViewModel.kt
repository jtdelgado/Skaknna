package com.skaknna.viewmodel

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Estado de autenticación del usuario.
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val userId: String, val displayName: String?, val email: String?) : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * ViewModel para gestionar la autenticación con Google usando Credential Manager e integración con Firebase.
 * 
 * Arquitectura MVVM:
 * - Separa la lógica de autenticación de la UI
 * - Maneja el flujo: Credential Manager -> Google ID Token -> Firebase Auth
 * - Expone StateFlow para observación reactiva desde Compose
 * 
 * @param context Contexto de la aplicación (necesario para Credential Manager)
 * @param webClientId Web Client ID de Google Cloud Console
 */
class AuthViewModel(
    private val context: Context,
    private val webClientId: String = "" // Obtener de BuildConfig en MainActivity
) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val credentialManager: CredentialManager = CredentialManager.create(context)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = firebaseAuth.currentUser
    val isUserLoggedIn: Boolean
        get() = firebaseAuth.currentUser != null

    init {
        // Si hay un usuario logeado, actualizar el estado
        if (_currentUser != null) {
            _authState.value = AuthState.Success(
                userId = _currentUser.uid,
                displayName = _currentUser.displayName,
                email = _currentUser.email
            )
        }
    }

    /**
     * Inicia el flujo de autenticación con Google mediante Credential Manager.
     * 
     * Flujo:
     * 1. Solicita credenciales usando Credential Manager
     * 2. Extrae el token de ID de Google
     * 3. Autentica en Firebase usando GoogleAuthProvider
     * 4. Actualiza el estado de autenticación
     * 
     * @param activity Activity necesaria para mostrar el diálogo de autenticación
     */
    fun signInWithGoogle(activity: Activity) {
        if (webClientId.isEmpty()) {
            _authState.value = AuthState.Error("WEB_CLIENT_ID no configurado. Verifica GOOGLE_SIGNIN_SETUP.md")
            return
        }

        viewModelScope.launch(Dispatchers.Main) {
            try {
                _authState.value = AuthState.Loading

                // Paso 1: Crear opción de Google ID
                val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)  // Permite nuevas cuentas
                    .setServerClientId(webClientId)
                    .build()

                // Paso 2: Crear solicitud de credenciales
                val request: GetCredentialRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                // Paso 3: Solicitar credenciales (muestra el diálogo de selección de cuenta)
                val credentialResponse = credentialManager.getCredential(
                    context = activity,
                    request = request
                )

                // Paso 4: Procesar la respuesta
                handleSignInResult(credentialResponse)

            } catch (e: GetCredentialException) {
                Log.e(TAG, "Error en Credential Manager: ${e.message}", e)
                _authState.value = AuthState.Error(
                    e.message ?: "Error desconocido durante la autenticación"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error en signInWithGoogle: ${e.message}", e)
                _authState.value = AuthState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Procesa la respuesta de Credential Manager y autentica con Firebase.
     * 
     * @param credentialResponse Respuesta de Credential Manager
     */
    private suspend fun handleSignInResult(credentialResponse: androidx.credentials.GetCredentialResponse) {
        val credential = credentialResponse.credential

        // Verificar que sea un custom credential de Google
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken

            // Autenticar con Firebase
            authenticateWithFirebase(idToken)
        } else {
            _authState.value = AuthState.Error("Tipo de credencial no soportado")
        }
    }

    /**
     * Autentica el token de ID de Google con Firebase Authentication.
     * 
     * @param idToken Token de ID de Google obtenido de Credential Manager
     */
    private suspend fun authenticateWithFirebase(idToken: String) {
        try {
            val authCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(authCredential).await()

            val user = authResult.user
            if (user != null) {
                _authState.value = AuthState.Success(
                    userId = user.uid,
                    displayName = user.displayName,
                    email = user.email
                )
                Log.d(TAG, "Autenticación exitosa. UID: ${user.uid}")
            } else {
                _authState.value = AuthState.Error("Usuario no obtenido después de autenticación")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al autenticar con Firebase: ${e.message}", e)
            _authState.value = AuthState.Error(
                e.message ?: "Error al autenticar con Firebase"
            )
        }
    }

    /**
     * Cierra sesión del usuario tanto en Firebase como en Credential Manager.
     */
    fun signOut() {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                firebaseAuth.signOut()
                _authState.value = AuthState.Idle
                Log.d(TAG, "Sesión cerrada exitosamente")
            } catch (e: Exception) {
                Log.e(TAG, "Error al cerrar sesión: ${e.message}", e)
                _authState.value = AuthState.Error(e.message ?: "Error al cerrar sesión")
            }
        }
    }
}
