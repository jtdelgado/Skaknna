package com.skaknna.ui.components.auth

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skaknna.R
import com.skaknna.ui.theme.GoldenYellow
import com.skaknna.ui.theme.WarmWhite
import com.skaknna.ui.theme.WoodDark
import com.skaknna.ui.theme.WoodMedium
import com.skaknna.viewmodel.AuthState
import com.skaknna.viewmodel.AuthViewModel

/**
 * Componente de botón de login con Google.
 * 
 * Gestiona:
 * - Estado de carga (muestra spinner)
 * - Estado de éxito (muestra información del usuario)
 * - Estado de error (muestra mensaje de error con botón de reintento)
 * 
 * @param viewModel ViewModel de autenticación
 * @param onLoginSuccess Callback ejecutado cuando la autenticación es exitosa
 * @param modifier Modificador de Compose opcional
 */
@Composable
fun GoogleLoginButton(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authState = viewModel.authState.collectAsState()
    val activity = LocalActivity.current

    when (val state = authState.value) {
        is AuthState.Idle -> {
            GoogleLoginButtonContent(
                isLoading = false,
                onLoginClick = {
                    activity?.let { viewModel.signInWithGoogle(it) }
                },
                modifier = modifier
            )
        }

        is AuthState.Loading -> {
            GoogleLoginButtonContent(
                isLoading = true,
                onLoginClick = {},
                modifier = modifier
            )
        }

        is AuthState.Success -> {
            UserProfileCard(
                displayName = state.displayName ?: "Usuario",
                email = state.email ?: "Sin correo",
                userId = state.userId,
                onLogout = { viewModel.signOut() },
                modifier = modifier
            )
            // Ejecutar callback de éxito
            LaunchedEffect(Unit) {
                onLoginSuccess()
            }
        }

        is AuthState.Error -> {
            ErrorCard(
                message = state.message,
                onRetry = {
                    activity?.let { viewModel.signInWithGoogle(it) }
                },
                onDismiss = { viewModel.signOut() },
                modifier = modifier
            )
        }
    }
}

/**
 * Contenido del botón de login (antes de autenticación).
 */
@Composable
private fun GoogleLoginButtonContent(
    isLoading: Boolean,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onLoginClick,
        enabled = !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color.Black.copy(alpha = 0.15f)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = WoodMedium,
            contentColor = GoldenYellow,
            disabledContainerColor = WoodDark,
            disabledContentColor = WarmWhite.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isLoading) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = GoldenYellow,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Autenticando...",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google Logo",
                        tint = GoldenYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Iniciar sesión con Google",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            )
        }
    }
}

/**
 * Tarjeta que muestra la información del usuario logeado.
 */
@Composable
private fun UserProfileCard(
    displayName: String,
    email: String,
    userId: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .border(2.dp, GoldenYellow, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = WoodDark,
            contentColor = WarmWhite
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Encabezado con estado exitoso
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✓ Sesión Iniciada",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = GoldenYellow
                )
            }

            Divider(color = WoodMedium)

            // Información del usuario
            InfoRow(label = "Nombre", value = displayName)
            InfoRow(label = "Correo", value = email)
            InfoRow(label = "ID Usuario", value = userId.take(8) + "...")

            Spacer(modifier = Modifier.height(8.dp))

            // Botón de logout
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Fila de información (etiqueta + valor).
 */
@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = GoldenYellow
        )
        Text(
            text = value,
            color = WarmWhite.copy(alpha = 0.8f)
        )
    }
}

/**
 * Tarjeta de error que permite reintentar o descartar.
 */
@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .border(2.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = WoodDark,
            contentColor = WarmWhite
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Encabezado con estado de error
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✗ Error de Autenticación",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.error
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Mensaje de error
            Text(
                text = message,
                fontSize = 12.sp,
                color = WarmWhite.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            )

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldenYellow,
                        contentColor = WoodDark
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reintentar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Descartar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

// Necesario para recomposición cuando cambia el estado
@Composable
private fun LaunchedEffect(key: Any?, block: suspend () -> Unit) {
    androidx.compose.runtime.LaunchedEffect(key) {
        block()
    }
}
