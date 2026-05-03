package com.skaknna.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.skaknna.ui.screens.AnalysisScreen
import com.skaknna.ui.screens.DashboardScreen
import com.skaknna.ui.screens.EditorScreen
import com.skaknna.ui.screens.ScannerScreen
import com.skaknna.ui.screens.SettingsScreen
import com.skaknna.ui.screens.LoginScreen
import com.skaknna.viewmodel.BoardViewModel
import com.skaknna.viewmodel.BoardViewModelFactory
import com.skaknna.viewmodel.SettingsViewModel
import com.skaknna.viewmodel.SettingsViewModelFactory
import com.skaknna.viewmodel.AuthViewModelFactory
import com.skaknna.viewmodel.AuthViewModel
import com.skaknna.viewmodel.AuthState

@Composable
fun AppNavigation(
    paddingValues: PaddingValues,
    boardViewModelFactory: BoardViewModelFactory,
    settingsViewModelFactory: SettingsViewModelFactory,
    authViewModelFactory: AuthViewModelFactory
) {
    val navController = rememberNavController()
    val boardViewModel: BoardViewModel = viewModel(factory = boardViewModelFactory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = settingsViewModelFactory)
    val authViewModel: AuthViewModel = viewModel(factory = authViewModelFactory)
    
    val authState = authViewModel.authState.collectAsState()
    val startDestination = if (authState.value is AuthState.Success) "dashboard" else "login"

    NavHost(
        navController = navController, 
        startDestination = startDestination,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToSettings = { navController.navigate("settings") },
                viewModel = authViewModel
            )
        }
        composable("dashboard") {
            DashboardScreen(
                viewModel = boardViewModel,
                authViewModel = authViewModel,
                onNavigateToScanner = { navController.navigate("scanner") },
                onNavigateToEditor = { navController.navigate("editor") },
                onNavigateToAnalysis = { navController.navigate("analysis") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToLogin = { 
                    navController.navigate("login") { 
                        popUpTo(0) { inclusive = true } 
                    } 
                }
            )
        }
        composable("scanner") {
            ScannerScreen(
                onValidationComplete = { fen ->
                    boardViewModel.updateFen(fen)
                    // Navigate to editor directly after scan
                    navController.navigate("editor") {
                        popUpTo("scanner") { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("editor") {
            val editorUserId = (authViewModel.authState.collectAsState().value as? AuthState.Success)?.userId
            EditorScreen(
                viewModel = boardViewModel,
                onSaveBoard = { boardName ->
                    boardViewModel.saveBoard(boardName, editorUserId)
                    navController.navigate("analysis") {
                        popUpTo("editor") { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("analysis") {
            AnalysisScreen(
                viewModel = boardViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
