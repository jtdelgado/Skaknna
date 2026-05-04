package com.skaknna.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
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

    val startDestination = "dashboard"

    // ── Double-back / black-screen guard ────────────────────────────────────
    // Tracks whether a popBackStack() is already in flight so that two rapid
    // back-presses do not drain the entire back stack and leave a black screen.
    var isNavigatingBack by remember { mutableStateOf(false) }

    // Observe the current route so we can block pops at the root destination.
    val currentBackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackEntry?.destination?.route

    /**
     * Safe pop: no-ops when already at startDestination or when a previous pop
     * is still being processed.
     */
    fun safePopBackStack() {
        if (isNavigatingBack) return               // debounce rapid double-tap
        if (currentRoute == startDestination) return // never pop the root screen
        if (navController.previousBackStackEntry == null) return // Prevent popping to black screen
        isNavigatingBack = true
        navController.popBackStack()
        // Reset the flag after the frame — Compose's LaunchedEffect can't be used
        // here (non-composable context) so we rely on the navController callback.
        navController.addOnDestinationChangedListener { _, _, _ ->
            isNavigatingBack = false
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(paddingValues)
    ) {
        dialog("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToSettings = {
                    navController.popBackStack()
                    navController.navigate("settings")
                },
                onNavigateBack = { safePopBackStack() },
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
                    navController.navigate("editor") {
                        popUpTo("scanner") { inclusive = true }
                    }
                },
                onNavigateBack = { safePopBackStack() }
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
                onNavigateBack = { safePopBackStack() }
            )
        }
        composable("analysis") {
            AnalysisScreen(
                viewModel = boardViewModel,
                onNavigateBack = { safePopBackStack() },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { safePopBackStack() }
            )
        }
    }
}
