package com.skaknna.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skaknna.R
import com.skaknna.ui.components.auth.GoogleLoginButton
import com.skaknna.ui.theme.GoldenYellow
import com.skaknna.ui.theme.WarmWhite
import com.skaknna.ui.theme.WoodDark
import com.skaknna.ui.theme.WoodLight
import com.skaknna.viewmodel.AuthViewModel

/**
 * Pantalla de login con Google.
 * 
 * Características:
 * - Interfaz atractiva con temática de madera (acorde al ajedrez)
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
    viewModel: AuthViewModel = viewModel()
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        WoodLight,
                        WoodDark
                    )
                )
            )
    ) {
        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Encabezado con botón de configuración
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(48.dp))
                
                Text(
                    text = stringResource(id = R.string.login_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = GoldenYellow,
                    textAlign = TextAlign.Center
                )
                
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(id = R.string.settings_title),
                        tint = GoldenYellow,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Contenido central
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icono/Logo
                Card(
                    modifier = Modifier
                        .size(80.dp)
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = GoldenYellow.copy(alpha = 0.1f)
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
                            color = GoldenYellow
                        )
                    }
                }

                // Título principal
                Text(
                    text = stringResource(id = R.string.login_welcome),
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = WarmWhite,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
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

                Spacer(modifier = Modifier.height(48.dp))

                // Botón de login
                GoogleLoginButton(
                    viewModel = viewModel,
                    onLoginSuccess = onLoginSuccess,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Información adicional
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = WoodDark.copy(alpha = 0.8f)
                    ),
                    shape = MaterialTheme.shapes.medium
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
                Divider(color = GoldenYellow.copy(alpha = 0.3f))
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
            color = GoldenYellow,
            modifier = Modifier.padding(top = 2.dp)
        )
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = GoldenYellow
            )
            Text(
                text = description,
                fontSize = 10.sp,
                color = WarmWhite.copy(alpha = 0.7f)
            )
        }
    }
}
