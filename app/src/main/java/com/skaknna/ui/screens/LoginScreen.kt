package com.skaknna.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skaknna.R
import com.skaknna.ui.components.auth.GoogleLoginButton
import com.skaknna.ui.theme.*
import com.skaknna.viewmodel.AuthViewModel

/**
 * Pantalla de login con Google.
 * 
 * Características:
 * - Interfaz atractiva con temática moderna y minimalista
 * - Autenticación moderna con Credential Manager
 * - Estados de carga y error
 * - Información del usuario logeado
 * 
 * @param onLoginSuccess Callback cuando la autenticación es exitosa
 * @param onNavigateToSettings Callback para navegar a settings
 * @param viewModel ViewModel de autenticación (inyectado por defecto)
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Transparent)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .align(Alignment.Center)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(28.dp), clip = false),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceGreen)
        ) {
            // Contenido principal
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Encabezado con botón de cerrar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.button_cancel),
                        tint = PrimaryGold,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Text(
                    text = stringResource(id = R.string.login_title),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = PrimaryGold,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Contenido central
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Icono/Logo
                Card(
                    modifier = Modifier
                        .size(80.dp)
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryGold.copy(alpha = 0.1f)
                    ),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "♔",
                            fontSize = 48.sp,
                            color = PrimaryGold
                        )
                    }
                }

                // Título principal
                Text(
                    text = stringResource(id = R.string.login_welcome),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 28.sp,
                    color = WarmWhite,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.headlineLarge
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Descripción
                Text(
                    text = stringResource(id = R.string.login_description),
                    fontSize = 14.sp,
                    color = WarmWhite.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botón de login
                GoogleLoginButton(
                    viewModel = viewModel,
                    onLoginSuccess = onLoginSuccess,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onNavigateBack) {
                    Text(
                        text = "Continuar sin iniciar sesión",
                        color = PrimaryGold.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Información adicional
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceGreen
                    ),
                    shape = MaterialTheme.shapes.medium,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineColor)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoBullet(
                            title = stringResource(id = R.string.login_feature_1_title),
                            description = stringResource(id = R.string.login_feature_1_desc)
                        )
                        InfoBullet(
                            title = stringResource(id = R.string.login_feature_2_title),
                            description = stringResource(id = R.string.login_feature_2_desc)
                        )
                        InfoBullet(
                            title = stringResource(id = R.string.login_feature_3_title),
                            description = stringResource(id = R.string.login_feature_3_desc)
                        )
                    }
                }
            }

            // Pie de página
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalDivider(color = OutlineColor)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.login_privacy_notice),
                    fontSize = 10.sp,
                    color = WarmWhite.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }
        }
    }
}

/**
 * Componente de viñeta informativa.
 */
@Composable
private fun InfoBullet(
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            fontSize = 16.sp,
            color = PrimaryGold,
            modifier = Modifier.padding(top = 2.dp)
        )
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = PrimaryGold
            )
            Text(
                text = description,
                fontSize = 10.sp,
                color = WarmWhite.copy(alpha = 0.7f)
            )
        }
    }
}
