package com.skaknna.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
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
import com.skaknna.viewmodel.BoardViewModel
import com.skaknna.viewmodel.BoardViewModelFactory
import com.skaknna.viewmodel.SettingsViewModel
import com.skaknna.viewmodel.SettingsViewModelFactory

@Composable
fun AppNavigation(
    paddingValues: PaddingValues,
    boardViewModelFactory: BoardViewModelFactory,
    settingsViewModelFactory: SettingsViewModelFactory
) {
    val navController = rememberNavController()
    // Shared ViewModels for the session
    val boardViewModel: BoardViewModel = viewModel(factory = boardViewModelFactory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = settingsViewModelFactory)

    NavHost(
        navController = navController, 
        startDestination = "dashboard",
        modifier = Modifier.padding(paddingValues)
    ) {
        composable("dashboard") {
            DashboardScreen(
                viewModel = boardViewModel,
                onNavigateToScanner = { navController.navigate("scanner") },
                onNavigateToEditor = { navController.navigate("editor") },
                onNavigateToAnalysis = { navController.navigate("analysis") },
                onNavigateToSettings = { navController.navigate("settings") }
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
            EditorScreen(
                viewModel = boardViewModel,
                onSaveBoard = { boardName ->
                    boardViewModel.saveBoard(boardName)
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
